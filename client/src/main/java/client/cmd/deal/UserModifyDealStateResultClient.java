package client.cmd.deal;

import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserModifyDealStateResultClient implements ICmd<GameMsg.UserModifyDealStateResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserModifyDealStateResult userModifyDealStateResult) {

        MyUtil.checkIsNull(ctx, userModifyDealStateResult);

        Role role = Role.getInstance();

        boolean isSuccess = userModifyDealStateResult.getIsSuccess();
        if (!isSuccess){

            GameMsg.DealFailCmd dealFailCmd = GameMsg.DealFailCmd.newBuilder().build();
            ctx.writeAndFlush(dealFailCmd);

        }else {
            role.getDEAL_CLIENT().setDealState(true);
            System.out.println("切换交易模块;");
        }

    }
}
