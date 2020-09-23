package server.cmdhandler.task.listener;

import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.model.PlayTask;
import server.model.User;
import server.scene.GameData;

/**
 * @author 张丰博
 * 任务: 击杀 xx 只怪
 */
@Component
@Slf4j
public class KillMonsterTask implements Task {

    /**
     * 击杀 XXX 怪 x 只;
     * @param user
     */
    @Override
    public void taskHandle(User user) {

        Integer currTaskId = user.getPlayTask().getCurrTaskId();

        server.model.task.Task task = GameData.getInstance().getTaskMap().get(currTaskId);
        if (!(user.getCurSceneId().equals(task.getSceneId()) && currTaskId.equals(user.getPlayTask().getCurrTaskId()))) {
            //场景不对或任务id不对时，直接结束
            return;
        }

        //判断是否击杀
        PlayTask playTask = user.getPlayTask();
        playTask.setNumber(playTask.getNumber() + 1);

        GameMsg.TaskProgressResult taskProgressResult = GameMsg.TaskProgressResult.newBuilder()
                .setNumber(playTask.getNumber())
                .build();
        user.getCtx().writeAndFlush(taskProgressResult);

        //击杀个数达到任务要求
        if (playTask.getNumber().equals(task.getNumber())) {
            //完成任务
            playTask.setCurrTaskCompleted(true);

            log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());
            GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
            user.getCtx().writeAndFlush(taskCompletedResult);
        }
        log.info("用户 {} 已击杀 {} 只怪;", user.getUserName(), playTask.getNumber());
    }
}
