package client.cmd.deal;

import client.cmd.ICmd;
import client.model.Role;
import client.model.deal.PlayDealClient;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class AskTargetUserResultClient implements ICmd<GameMsg.AskTargetUserResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.AskTargetUserResult askTargetUserResult) {

        MyUtil.checkIsNull(ctx, askTargetUserResult);
        Role role = Role.getInstance();

        String originateName = askTargetUserResult.getOriginateName();
        int originateId = askTargetUserResult.getOriginateId();

        PlayDealClient deal_client = role.getDEAL_CLIENT();
        deal_client.setOriginateId(originateId);

        System.out.println("用户: "+originateName +" 发起交易请求;");

    }
}
