package client.cmd.duplicate;

import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class BossAllKillResultClient implements ICmd<GameMsg.BossAllKillResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.BossAllKillResult bossAllKillResult) {

        MyUtil.checkIsNull(ctx, bossAllKillResult);
        Role role = Role.getInstance();

        GameMsg.DuplicateFinishCmd duplicateFinishCmd = GameMsg.DuplicateFinishCmd.newBuilder().build();
        ctx.writeAndFlush(duplicateFinishCmd);


    }
}
