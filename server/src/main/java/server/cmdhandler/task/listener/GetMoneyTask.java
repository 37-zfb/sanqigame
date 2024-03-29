package server.cmdhandler.task.listener;

import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.model.PlayTask;
import server.model.User;
import server.scene.GameData;

/**
 * @author 张丰博
 *
 * 任务: 获得 xx 钱
 */
@Component
@Slf4j
public class GetMoneyTask implements Task {
    @Override
    public void taskHandle(User user) {
        PlayTask playTask = user.getPlayTask();
        server.model.task.Task task = GameData.getInstance().getTaskMap().get(playTask.getCurrTaskId());

        int targetMoney = task.getNumber();


        GameMsg.TaskProgressResult taskProgressResult = GameMsg.TaskProgressResult.newBuilder()
                .setNumber(user.getMoney())
                .build();
        user.getCtx().writeAndFlush(taskProgressResult);
        playTask.setNumber(user.getMoney());

        if (user.getMoney() < targetMoney ||  playTask.isCurrTaskCompleted()) {
            return;
        }

        playTask.setCurrTaskCompleted(true);

        log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());
        GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
        user.getCtx().writeAndFlush(taskCompletedResult);

    }
}
