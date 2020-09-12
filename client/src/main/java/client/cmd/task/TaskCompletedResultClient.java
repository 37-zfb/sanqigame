package client.cmd.task;

import client.cmd.ICmd;
import client.model.Role;
import client.model.server.scene.Npc;
import client.model.server.task.Task;
import client.model.task.PlayTaskClient;
import client.scene.GameData;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class TaskCompletedResultClient implements ICmd<GameMsg.TaskCompletedResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.TaskCompletedResult taskCompletedResult) {

        MyUtil.checkIsNull(ctx, taskCompletedResult);
        Role role = Role.getInstance();

        GameData gameData = GameData.getInstance();
        Task task = gameData.getTaskMap().get(role.getPlayTaskClient().getCurrTaskId());
        if (task.getNpcId() != null && task.getNpcId() != 0) {
            Npc npc = gameData.getSceneMap().get(role.getCurrSceneId()).getNpcMap().get(task.getNpcId());
            System.out.println(npc.getName() + ": " + task.getDialogue());
        }

        PlayTaskClient playTaskClient = role.getPlayTaskClient();
        playTaskClient.setCompleted(true);




    }
}
