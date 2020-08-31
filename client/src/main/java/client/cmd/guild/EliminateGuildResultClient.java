package client.cmd.guild;

import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import util.MyUtil;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public class EliminateGuildResultClient implements ICmd<GameMsg.EliminateGuildResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.EliminateGuildResult eliminateGuildResult) {

        MyUtil.checkIsNull(ctx, eliminateGuildResult);
        Role role = Role.getInstance();

        role.setPlayGuildClient(null);

        GameMsg.ModifyGuildStateCmd modifyGuildStateCmd = GameMsg.ModifyGuildStateCmd.newBuilder().build();
        ctx.writeAndFlush(modifyGuildStateCmd);
    }
}
