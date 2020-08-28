package client.cmd.guild;

import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import client.thread.CmdThread;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
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

        System.out.println("查看用户;");
        System.out.println("0、退出");
        Scanner scanner = new Scanner(System.in);
        int nextInt = scanner.nextInt();
        if (nextInt == 0) {
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        }else {
            // 查看玩家信息
            GameMsg.LookGuildMemberInfoCmd build = GameMsg.LookGuildMemberInfoCmd.newBuilder()
                    .setUserId(nextInt)
                    .build();
            ctx.writeAndFlush(build);
        }

    }
}
