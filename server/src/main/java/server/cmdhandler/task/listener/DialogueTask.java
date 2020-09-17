package server.cmdhandler.task.listener;

import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.model.PlayTask;
import server.model.User;
import server.scene.GameData;

/**
 * @author 张丰博
 * 任务: 与NPC对话
 */
@Slf4j
@Component
public class DialogueTask implements Task {

    /**
     * 任务类型1: 对话
     *
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

        PlayTask playTask = user.getPlayTask();

        if (playTask.isCurrTaskCompleted()) {
            return;
        }

        playTask.setCurrTaskCompleted(true);


        log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());

        GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
        user.getCtx().writeAndFlush(taskCompletedResult);
    }
}
