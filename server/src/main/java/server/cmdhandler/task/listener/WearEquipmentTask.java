package server.cmdhandler.task.listener;

import entity.db.UserEquipmentEntity;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.model.PlayTask;
import server.model.User;
import server.scene.GameData;

/**
 * @author 张丰博
 * 任务: 穿戴装备
 */
@Component
@Slf4j
public class WearEquipmentTask implements Task {
    @Override
    public void taskHandle(User user) {
        PlayTask playTask = user.getPlayTask();
        server.model.task.Task task = GameData.getInstance().getTaskMap().get(playTask.getCurrTaskId());

        int targetNumber = task.getNumber();
        int currSize = 0;
        for (UserEquipmentEntity userEquipmentEntity : user.getUserEquipmentArr()) {
            if (userEquipmentEntity != null) {
                currSize++;
            }
        }

        GameMsg.TaskProgressResult taskProgressResult = GameMsg.TaskProgressResult.newBuilder()
                .setNumber(currSize)
                .build();
        user.getCtx().writeAndFlush(taskProgressResult);
        playTask.setNumber(currSize);

        if (targetNumber != currSize  || playTask.isCurrTaskCompleted()) {
            return;
        }

        playTask.setCurrTaskCompleted(true);

        log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());
        GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
        user.getCtx().writeAndFlush(taskCompletedResult);

    }
}
