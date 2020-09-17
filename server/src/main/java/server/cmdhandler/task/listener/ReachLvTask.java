package server.cmdhandler.task.listener;

import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.model.PlayTask;
import server.model.User;
import server.scene.GameData;

/**
 * @author 张丰博
 * 任务: 达到 xx 等级
 */
@Component
@Slf4j
public class ReachLvTask implements Task {
    @Override
    public void taskHandle(User user) {
        PlayTask playTask = user.getPlayTask();
        server.model.task.Task task = GameData.getInstance().getTaskMap().get(playTask.getCurrTaskId());

        int targetLv = task.getNumber();
        if (user.getLv() >= targetLv && !playTask.isCurrTaskCompleted()) {
            playTask.setCurrTaskCompleted(true);

            if (task.getNum() >= 1) {
                return;
            }
            task.setNum(task.getNum() + 1);

            log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());
            GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
            user.getCtx().writeAndFlush(taskCompletedResult);


        }

    }
}
