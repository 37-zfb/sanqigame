package client.cmd.arena;

import client.cmd.ICmd;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;

/**
 * @author 张丰博
 */
public class UserAttackResultClient implements ICmd<GameMsg.UserAttackResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserAttackResult userAttackResult) {


    }
}
