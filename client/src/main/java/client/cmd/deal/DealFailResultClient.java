package client.cmd.deal;

import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class DealFailResultClient implements ICmd<GameMsg.DealFailResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.DealFailResult dealFailResult) {

        MyUtil.checkIsNull(ctx, dealFailResult);
        Role role = Role.getInstance();
        role.getDEAL_CLIENT().setDealState(false);
        System.out.println("交易建立失败;");
    }
}
