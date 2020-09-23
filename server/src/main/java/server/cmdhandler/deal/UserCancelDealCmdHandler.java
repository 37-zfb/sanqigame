package server.cmdhandler.deal;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.UserManager;
import server.cmdhandler.ICmdHandler;
import server.model.Deal;
import server.model.User;
import util.MyUtil;

/**
 * @author 张丰博
 * 用户取消交易
 */
@Component
@Slf4j
public class UserCancelDealCmdHandler implements ICmdHandler<GameMsg.UserCancelDealCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserCancelDealCmd userCancelDealCmd) {

        MyUtil.checkIsNull(ctx, userCancelDealCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        Deal deal = user.getDeal();
        if (deal == null || deal.getInitiatorId() == null || deal.getTargetId() == null) {
            return;
        }


        user.setDeal(null);
        log.info("用户 {} 取消交易;", user.getUserName());

        User targetUser = null;
        if (user.getUserId() == deal.getInitiatorId()) {
            targetUser = UserManager.getUserById(deal.getTargetId());
        }
        if (user.getUserId() == deal.getTargetId()) {
            targetUser = UserManager.getUserById(deal.getInitiatorId());
        }


        GameMsg.UserCancelDealResult userCancelDealResult = GameMsg.UserCancelDealResult.newBuilder()
                .setUserId(user.getUserId())
                .build();
        ctx.writeAndFlush(userCancelDealResult);

        if (targetUser == null || targetUser.getDeal() == null) {
            return;
        }
        targetUser.getCtx().writeAndFlush(userCancelDealResult);
    }
}
