package client.cmd.deal;

import client.cmd.ICmd;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserResetMoneyResultClient implements ICmd<GameMsg.UserResetMoneyResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserResetMoneyResult userResetMoneyResult) {

        MyUtil.checkIsNull(ctx, userResetMoneyResult);

        int money = userResetMoneyResult.getMoney();
        System.out.println("对方重置金币: "+money);

    }
}
