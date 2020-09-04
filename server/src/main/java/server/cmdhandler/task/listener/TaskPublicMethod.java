package server.cmdhandler.task.listener;

import constant.ProfessionConst;
import entity.db.DbTaskEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.model.PlayTask;
import server.model.User;
import server.model.task.Task;
import server.scene.GameData;
import server.service.TaskService;
import type.TaskType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * @author 张丰博
 */
@Component
@Slf4j
public class TaskPublicMethod {

    @Autowired
    private TaskService taskService;


    /**
     * 领取任务
     *
     * @param ctx
     * @param taskId
     */
    public void receiveTask(ChannelHandlerContext ctx, Integer taskId) {
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayTask playTask = user.getPlayTask();
        if (playTask.getCompletedTaskId() >= taskId) {
            //此时领取的任务为已做过的
            return;
        }

        Task task = GameData.getInstance().getTaskMap().get(taskId);
        if (task == null) {
            // 此时任务已做完
            playTask.setHaveTask(false);

            DbTaskEntity taskEntity = getTaskEntity(user);
            taskService.modifyTaskState(taskEntity);

            return;
        }

        playTask.setCurrTaskId(taskId);
        log.info("用户 {} 领取 {} 任务", user.getUserName(), task.getTaskName());

        GameMsg.ReceiveTaskResult receiveTaskResult = GameMsg.ReceiveTaskResult.newBuilder()
                .setTaskId(taskId)
                .build();
        ctx.writeAndFlush(receiveTaskResult);

        //加入公会任务
        if (taskId.equals(3)){
            TaskListener.getTaskListener().addGuild3(user);
        }

//        if ()

    }

    /**
     * 增加经验
     *
     * @param experience
     * @param user
     */
    public void addExperience(Integer experience, User user) {
        if ((user.getExperience() + experience) < ProfessionConst.NEED_EXPERIENCE) {
            //长经验
            log.info("用户 {} 获得 {} 经验;", user.getUserName(), experience);
            user.setExperience(user.getExperience() + experience);
            return;
        }
        //升级
        user.setExperience(user.getExperience() + experience - ProfessionConst.NEED_EXPERIENCE);
        user.setLv(user.getLv() + 1);
        log.info("用户 {} 获得 {} 经验,当前 LV {} ;", user.getUserName(), experience, user.getLv());
        GameMsg.UserUpLvResult userUpLvResult = GameMsg.UserUpLvResult.newBuilder()
                .setLv(user.getLv())
                .build();
        user.getCtx().writeAndFlush(userUpLvResult);
    }

    /**
     * 获得task实体
     *
     * @param user
     * @return
     */
    public DbTaskEntity getTaskEntity(User user) {
        DbTaskEntity dbTaskEntity = new DbTaskEntity();
        dbTaskEntity.setUserId(user.getUserId());
        dbTaskEntity.setCompletedTask(user.getPlayTask().isHaveTask() ?user.getPlayTask().getCompletedTaskId(): TaskType.NonTask.getTaskCode());
        dbTaskEntity.setCurrTaskCompleted(user.getPlayTask().isCurrTaskCompleted() ? TaskType.CurrTaskCompleted.getTaskCode() : TaskType.CurrTaskUnCompleted.getTaskCode());
        dbTaskEntity.setCurrTask(user.getPlayTask().getCurrTaskId());

        if (user.getPlayTask().getCurrTaskId().equals(2)){
            dbTaskEntity.setKillNumber(user.getPlayTask().getKillNumber());
        }


        return dbTaskEntity;
    }


    public void listener(User user){

        PlayTask playTask = user.getPlayTask();
        if (!playTask.isHaveTask()){
            //没有任务
            return;
        }

        Integer currTaskId = playTask.getCurrTaskId();
        Method method = TaskListener.getTaskListener().listenerMethod.get(currTaskId);

        try {

            method.invoke(TaskListener.getTaskListener(), user);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


    }

}
