package client.cmd.task;

import client.cmd.ICmd;
import client.model.Role;
import client.model.task.PlayTaskClient;
import client.scene.GameData;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import type.TaskType;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class ReceiveTaskResultClient implements ICmd<GameMsg.ReceiveTaskResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.ReceiveTaskResult receiveTaskResult) {

        MyUtil.checkIsNull(ctx, receiveTaskResult);
        Role role = Role.getInstance();

        int taskId = receiveTaskResult.getTaskId();

        PlayTaskClient playTaskClient = role.getPlayTaskClient();
        if (taskId == TaskType.NonTask.getTaskCode()){
            playTaskClient.setCurrTaskId(taskId);
            System.out.println("任务全部完成;");
        }else {
            playTaskClient.setCompleted(false);
            playTaskClient.setCurrTaskId(taskId);
            System.out.println("领取任务: "+ GameData.getInstance().getTaskMap().get(taskId).getTaskName());
        }


//        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
    }
}
