package client.cmd.guild;

import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import client.thread.CmdThread;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import type.GuildMemberType;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class AppointMemberResultClient implements ICmd<GameMsg.AppointMemberResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.AppointMemberResult appointMemberResult) {
        MyUtil.checkIsNull(ctx, appointMemberResult);
        Role role = Role.getInstance();
        int positionId = appointMemberResult.getPositionId();
        role.getPlayGuildClient().setType(GuildMemberType.getRoleNameByRoleId(positionId));

        GameMsg.ModifyGuildPositionCmd modifyGuildPositionCmd = GameMsg.ModifyGuildPositionCmd.newBuilder()
                .setPositionId(positionId)
                .build();
        ctx.writeAndFlush(modifyGuildPositionCmd);

    }
}
