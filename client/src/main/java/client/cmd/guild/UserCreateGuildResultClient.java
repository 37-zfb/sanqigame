package client.cmd.guild;

import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import client.model.guild.PlayGuildClient;
import client.thread.CmdThread;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import type.GuildMemberType;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserCreateGuildResultClient implements ICmd<GameMsg.UserCreateGuildResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserCreateGuildResult userCreateGuildResult) {

        MyUtil.checkIsNull(ctx, userCreateGuildResult);

        String guildName = userCreateGuildResult.getGuildName();
        Role role = Role.getInstance();

        PlayGuildClient playGuildClient = new PlayGuildClient();
        playGuildClient.setType(GuildMemberType.President.getRoleName());
        playGuildClient.setGuildName(guildName);

        role.setPlayGuildClient(playGuildClient);


        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());

    }
}
