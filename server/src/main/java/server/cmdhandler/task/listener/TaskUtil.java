package server.cmdhandler.task.listener;

import constant.ProfessionConst;
import entity.db.DbTaskEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.GameServer;
import server.PublicMethod;
import server.model.PlayTask;
import server.model.User;
import server.model.task.Task;
import server.scene.GameData;
import server.service.TaskService;
import type.TaskType;


/**
 * @author 张丰博
 */
@Component
@Slf4j
public class TaskUtil {

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

            GameMsg.ReceiveTaskResult receiveTaskResult = GameMsg.ReceiveTaskResult.newBuilder()
                    .setTaskId(TaskType.NonTask.getTaskCode())
                    .build();
            ctx.writeAndFlush(receiveTaskResult);

            return;
        }
        task.setNum(0);
        playTask.setCurrTaskId(taskId);
        playTask.setNumber(0);
        log.info("用户 {} 领取 {} 任务", user.getUserName(), task.getTaskName());

        GameMsg.ReceiveTaskResult receiveTaskResult = GameMsg.ReceiveTaskResult.newBuilder()
                .setTaskId(taskId)
                .build();
        ctx.writeAndFlush(receiveTaskResult);


        if (TaskType.isContain(task.getTypeCode())) {
            listener(user);
        }

//        int[] code = {3, 5, 6, 7, 8, 9, 12};
//        for (int typeCode : code) {
//            if (task.getTypeCode() == typeCode) {
//                Method method = TaskListener.getTaskListener().listenerMethod.get(typeCode);
//                try {
//                    method.invoke(TaskListener.getTaskListener(), user);
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                }
//            }
//        }


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

        Task task = GameData.getInstance().getTaskMap().get(user.getPlayTask().getCurrTaskId());

        if (task != null && task.getTypeCode().equals(TaskType.LvType.getTaskCode())) {
            //此时任务: 提升等级
            listener(user);
        }
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
        dbTaskEntity.setCompletedTask(user.getPlayTask().isHaveTask() ? user.getPlayTask().getCompletedTaskId() : TaskType.NonTask.getTaskCode());
        dbTaskEntity.setCurrTaskCompleted(user.getPlayTask().isCurrTaskCompleted() ? TaskType.CurrTaskCompleted.getTaskCode() : TaskType.CurrTaskUnCompleted.getTaskCode());
        dbTaskEntity.setCurrTask(user.getPlayTask().getCurrTaskId());

        Task task = GameData.getInstance().getTaskMap().get(user.getPlayTask().getCurrTaskId());

        if (task.getTypeCode().equals(TaskType.KillMonsterType.getTaskCode())) {
            dbTaskEntity.setTaskProcess(user.getPlayTask().getNumber());
        }


        return dbTaskEntity;
    }


    public void listener(User user) {

        PlayTask playTask = user.getPlayTask();
        if (!playTask.isHaveTask()) {
            //没有任务
            return;
        }

        Integer currTaskId = playTask.getCurrTaskId();
        Task task = GameData.getInstance().getTaskMap().get(currTaskId);

        String clazzStr = "";
        for (TaskType taskType : TaskType.values()) {
            if (!task.getTypeCode().equals(taskType.getTaskCode())) {
                continue;
            }
            clazzStr = taskType.getTaskType();
        }

        if (clazzStr.equals("")) {
            return;
        }

        server.cmdhandler.task.listener.Task bean = null;
        try {
            bean = (server.cmdhandler.task.listener.Task) GameServer.APPLICATION_CONTEXT.getBean(Class.forName(clazzStr));
            bean.taskHandle(user);


        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }

}
