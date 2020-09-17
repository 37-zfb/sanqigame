package client.cmd.guild;

import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import client.CmdThread;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import type.GuildMemberType;
import util.MyUtil;

import java.util.List;
import java.util.Scanner;

/**
 * @author 张丰博
 */
public class ShowGuildMemberResultClient implements ICmd<GameMsg.ShowGuildMemberResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.ShowGuildMemberResult showGuildMemberResult) {

        MyUtil.checkIsNull(ctx, showGuildMemberResult);
        Role role = Role.getInstance();

        List<GameMsg.UserInfo> userInfoList = showGuildMemberResult.getUserInfoList();
        for (GameMsg.UserInfo userInfo : userInfoList) {
            System.out.println(userInfo.getUserId() + "、" + userInfo.getUserName() + "  " + (userInfo.getIsOnline() ? "在线;" : "离线;"));
        }
        Scanner scanner = new Scanner(System.in);

        if (role.isTransferPresident()) {
            role.setTransferPresident(false);
            System.out.println("选择玩家;");
            int userId = scanner.nextInt();
            GameMsg.TransferPresidentCmd transferPresidentCmd = GameMsg.TransferPresidentCmd.newBuilder()
                    .setUserId(userId)
                    .build();
            ctx.writeAndFlush(transferPresidentCmd);
        } else if (role.isEliminate()) {
            role.setEliminate(false);
            System.out.println("踢人:");
            System.out.println("0、退出");
            int nextInt = scanner.nextInt();
            scanner.nextLine();
            if (nextInt != 0) {
                GameMsg.EliminateGuildCmd build = GameMsg.EliminateGuildCmd.newBuilder()
                        .setUserId(nextInt)
                        .build();
                ctx.writeAndFlush(build);
            }
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());

        } else if (role.isAppoint()) {
            role.setAppoint(false);
            System.out.println("任命成员职位:");
            System.out.println("0、退出");
            int nextInt = scanner.nextInt();
            scanner.nextLine();
            if (nextInt != 0) {
                // 任命玩家
                System.out.println("职位;");
                if (role.getPlayGuildClient().getType().equals(GuildMemberType.President.getRoleName())) {
                    //会长
                    System.out.println(GuildMemberType.VicePresident.getRoleId() + "、" + GuildMemberType.VicePresident.getRoleName());
                }
                System.out.println(GuildMemberType.Elite.getRoleId() + "、" + GuildMemberType.Elite.getRoleName());
                System.out.println(GuildMemberType.Member.getRoleId() + "、" + GuildMemberType.Member.getRoleName());

                int anInt = scanner.nextInt();

                GameMsg.AppointMemberCmd build = GameMsg.AppointMemberCmd.newBuilder()
                        .setUserId(nextInt)
                        .setPositionId(anInt)
                        .build();
                ctx.writeAndFlush(build);
            }
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());

        } else {
            System.out.println("查看用户;");
            System.out.println("0、退出");
            int nextInt = scanner.nextInt();
            if (nextInt == 0) {
                CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
            } else {
                // 查看玩家信息
                GameMsg.LookGuildMemberInfoCmd build = GameMsg.LookGuildMemberInfoCmd.newBuilder()
                        .setUserId(nextInt)
                        .build();
                ctx.writeAndFlush(build);
            }
        }


    }
}
