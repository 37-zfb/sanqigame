package server.cmdhandler.deal;

import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.Deal;
import server.model.User;
import server.UserManager;
import util.MyUtil;

/**
 * @author 张丰博
 * 被请求玩家响应结果
 */
@Component
@Slf4j
public class DealTargetUserResponseCmdHandler implements ICmdHandler<GameMsg.DealTargetUserResponseCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.DealTargetUserResponseCmd dealTargetUserResponseCmd) {

        MyUtil.checkIsNull(ctx, dealTargetUserResponseCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        if (user.getDeal() != null) {
            //此时已经在交易状态
            throw new CustomizeException(CustomizeErrorCode.DEAL_STATE);
        }

        boolean isAgree = dealTargetUserResponseCmd.getIsAgree();
        int originateId = dealTargetUserResponseCmd.getOriginateId();
        User originateUser = UserManager.getUserById(originateId);
        if (originateUser == null) {
            throw new CustomizeException(CustomizeErrorCode.ORIGINATE_USER_NOT_FOUNT);
        }


        GameMsg.UserDealRequestResult.Builder newBuilder = GameMsg.UserDealRequestResult.newBuilder();
        if (!isAgree) {
            // 拒绝交易,
            log.info("用户 {} 拒绝 {} 交易;", user.getUserName(), originateUser.getUserName());
            GameMsg.UserDealRequestResult userDealRequestResult = newBuilder
                    .setIsAgree(false)
                    .setTargetUserId(user.getUserId())
                    .setTargetUserName(user.getUserName())
                    .build();

            ctx.writeAndFlush(userDealRequestResult);
            originateUser.getCtx().writeAndFlush(userDealRequestResult);
        } else {

            Deal deal = new Deal();
            deal.setTargetId(user.getUserId());
            user.setDeal(deal);

            log.info("用户 {} 同意 {} 交易;", user.getUserName(), originateUser.getUserName());
            GameMsg.UserDealRequestResult userDealRequestResult = newBuilder
                    .setIsAgree(true)
                    .setTargetUserId(user.getUserId())
                    .setTargetUserName(user.getUserName())
                    .build();
            ctx.writeAndFlush(userDealRequestResult);
            originateUser.getCtx().writeAndFlush(userDealRequestResult);
        }
    }
}
