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
public class UserEnterGuildResultClient implements ICmd<GameMsg.UserEnterGuildResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserEnterGuildResult userEnterGuildResult) {

        MyUtil.checkIsNull(ctx, userEnterGuildResult);
        Role role = Role.getInstance();

        GameMsg.Guild guild = userEnterGuildResult.getGuild();
        int guildId = guild.getGuildId();
        String guildName = guild.getGuildName();

        PlayGuildClient playGuildClient = new PlayGuildClient();
        playGuildClient.setGuildName(guildName);
        playGuildClient.setType(GuildMemberType.Member.getRoleName());

        role.setPlayGuildClient(playGuildClient);
        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
    }
}
