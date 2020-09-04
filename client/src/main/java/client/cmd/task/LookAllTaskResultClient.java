package client.cmd.task;

import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import client.model.server.props.Props;
import client.model.server.task.Task;
import client.scene.GameData;
import client.thread.CmdThread;
import entity.MailProps;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author 张丰博
 */
public class LookAllTaskResultClient implements ICmd<GameMsg.LookAllTaskResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.LookAllTaskResult lookAllTaskResult) {

        MyUtil.checkIsNull(ctx, lookAllTaskResult);
        Role role = Role.getInstance();

        int taskId = lookAllTaskResult.getTaskId();
        Task task = GameData.getInstance().getTaskMap().get(taskId);

        String taskName = task.getTaskName();
        String description = task.getDescription();
        int rewardMoney = task.getRewardMoney();
        List<MailProps> rewardProps = task.getRewardProps();
        System.out.println(taskId + "、" + taskName);
        System.out.println("描述: " + description);
        System.out.println("奖励: ");
        System.out.println("金币: " + rewardMoney);

        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        for (MailProps mailProps : rewardProps) {
            System.out.println(propsMap.get(mailProps.getPropsId()) + " " + mailProps.getNumber());
        }

        System.out.println("是否领取?");
        System.out.println("1、是;");
        System.out.println("2、否;");
        Scanner scanner = new Scanner(System.in);
        int anInt = scanner.nextInt();
        if (anInt == 1) {
            //领取任务
            GameMsg.ReceiveTaskCmd receiveTaskCmd = GameMsg.ReceiveTaskCmd.newBuilder()
                    .setTaskId(taskId)
                    .build();
            ctx.writeAndFlush(receiveTaskCmd);
        }
        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());

    }
}
