package client.cmd.task;

import client.cmd.ICmd;
import client.model.Role;
import client.model.server.scene.Npc;
import client.model.server.task.Task;
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

        System.out.println("获得奖励: ");
        System.out.println(task.getExperience() + "经验");
        task.getRewardProps().forEach(System.out::println);
        System.out.println(task.getRewardMoney() + " 金币");

        GameMsg.ReceiveTaskAwardCmd receiveTaskAwardCmd = GameMsg.ReceiveTaskAwardCmd.newBuilder()
                .setTaskId(task.getId())
                .build();
        ctx.writeAndFlush(receiveTaskAwardCmd);
    }
}
