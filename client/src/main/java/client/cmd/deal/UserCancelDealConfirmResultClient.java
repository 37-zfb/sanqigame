package client.cmd.deal;

import client.cmd.ICmd;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserCancelDealConfirmResultClient implements ICmd<GameMsg.UserCancelDealConfirmResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserCancelDealConfirmResult userCancelDealConfirmResult) {
        MyUtil.checkIsNull(ctx, userCancelDealConfirmResult);
        System.out.println("对方取消确定交易;");
        GameMsg.UserCancelDealConfirmCmd userCancelDealConfirmCmd =
                GameMsg.UserCancelDealConfirmCmd.newBuilder()
                        .build();
        ctx.writeAndFlush(userCancelDealConfirmCmd);
    }
}
