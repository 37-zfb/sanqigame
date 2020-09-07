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
public class TransferPresidentResultClient implements ICmd<GameMsg.TransferPresidentResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.TransferPresidentResult transferPresidentResult) {

        MyUtil.checkIsNull(ctx, transferPresidentResult);
        Role role = Role.getInstance();

        int userId = transferPresidentResult.getUserId();
        if (userId == role.getId()) {
            role.getPlayGuildClient().setType(GuildMemberType.President.getRoleName());
        } else {
            role.getPlayGuildClient().setType(GuildMemberType.Member.getRoleName());
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        }

    }
}
