package client.cmd.deal;

import client.cmd.ICmd;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserConfirmDealResultClient implements ICmd<GameMsg.UserConfirmDealResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserConfirmDealResult userConfirmDealResult) {

        MyUtil.checkIsNull(ctx,userConfirmDealResult);

        boolean isSuccess = userConfirmDealResult.getIsSuccess();
        if (isSuccess){
            // 双方都确认交易;
            GameMsg.SortOutDealCmd sortOutDealCmd = GameMsg.SortOutDealCmd.newBuilder().build();
            ctx.writeAndFlush(sortOutDealCmd);
        }else {
            System.out.println("对方确认交易;");
        }

    }
}
