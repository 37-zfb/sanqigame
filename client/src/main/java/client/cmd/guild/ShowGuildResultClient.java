package client.cmd.guild;

import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import client.CmdThread;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

import java.util.List;
import java.util.Scanner;

/**
 * @author 张丰博
 */
public class ShowGuildResultClient implements ICmd<GameMsg.ShowGuildResult> {

    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.ShowGuildResult showGuildResult) {

        MyUtil.checkIsNull(ctx, showGuildResult);
        Role role = Role.getInstance();

        List<GameMsg.Guild> guildList = showGuildResult.getGuildList();

        if (guildList.size() == 0){
            System.out.println("暂无公会;");
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
            return;
        }
        for (GameMsg.Guild guild : guildList) {
            System.out.println(guild.getGuildId() + "、" + guild.getGuildName());
        }
        System.out.println("请选择要加入的公会,或者0退出;");
        Scanner scanner = new Scanner(System.in);
        int nextInt = scanner.nextInt();
        if (nextInt == 0) {
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        } else {
            GameMsg.UserEnterGuildCmd userEnterGuildCmd = GameMsg.UserEnterGuildCmd.newBuilder()
                    .setGuildId(nextInt)
                    .build();
            ctx.writeAndFlush(userEnterGuildCmd);
        }

    }
}
