package client.cmd.auction;

import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class BiddingGoodsResultClient implements ICmd<GameMsg.BiddingGoodsResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.BiddingGoodsResult biddingGoodsResult) {

        MyUtil.checkIsNull(ctx,biddingGoodsResult);
        Role role = Role.getInstance();
        role.setMoney(role.getMoney()-biddingGoodsResult.getMoney());
    }
}
