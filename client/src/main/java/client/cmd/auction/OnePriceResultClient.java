package client.cmd.auction;

import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */

public class OnePriceResultClient implements ICmd<GameMsg.OnePriceResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.OnePriceResult onePriceResult) {

        MyUtil.checkIsNull(ctx,onePriceResult);
        Role role = Role.getInstance();
        role.setMoney(role.getMoney()-onePriceResult.getPrice());

    }
}
