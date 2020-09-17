package server.cmdhandler.task.listener;

import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.model.PlayTask;
import server.model.User;
import server.model.props.Props;
import server.scene.GameData;
import type.PropsType;

import java.util.Map;

/**
 * @author 张丰博
 * 任务: 获得 xx 件装备
 */
@Component
@Slf4j
public class GetEquipmentTask implements Task {
    @Override
    public void taskHandle(User user) {
        PlayTask playTask = user.getPlayTask();
        server.model.task.Task task = GameData.getInstance().getTaskMap().get(playTask.getCurrTaskId());

        int targetEquSize = task.getNumber();

        Map<Integer, Props> backpack = user.getBackpack();
        int equSize = 0;
        for (Props props : backpack.values()) {
            if (props.getPropsProperty().getType() == PropsType.Equipment) {
                equSize++;
            }
        }

        GameMsg.TaskProgressResult taskProgressResult = GameMsg.TaskProgressResult.newBuilder()
                .setNumber(equSize)
                .build();
        user.getCtx().writeAndFlush(taskProgressResult);
        playTask.setNumber(equSize);

        if (equSize < targetEquSize || playTask.isCurrTaskCompleted()) {
            return;
        }

        playTask.setCurrTaskCompleted(true);

        log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());
        GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
        user.getCtx().writeAndFlush(taskCompletedResult);

    }
}
