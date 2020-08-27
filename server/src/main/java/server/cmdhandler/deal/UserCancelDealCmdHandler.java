package server.cmdhandler.deal;

import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayDeal;
import server.model.User;
import server.model.UserManager;
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

        PlayDeal play_deal = user.getPLAY_DEAL();
        int targetId = play_deal.getTargetUserId().get();

        GameMsg.UserCancelDealResult.Builder newBuilder = GameMsg.UserCancelDealResult.newBuilder();
        if (targetId == 0) {
            // 此时不在交易状态
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_DEAL_STATUS);
        } else {
            // 此时在交易状态
            boolean isSuccess = play_deal.getTargetUserId().compareAndSet(targetId, 0);
            if (isSuccess) {
                // 成功
                play_deal.getPrepareProps().clear();
                play_deal.setPrepareMoney(0);
                newBuilder.setIsSuccess(true).setUserId(user.getUserId());
                log.info("用户 {} 取消交易;", user.getUserName());
            }else {
                newBuilder.setIsSuccess(false);
                log.info("用户 {} 取消交易失败;", user.getUserName());
            }
        }

        User targetUser = UserManager.getUserById(targetId);

        if (userCancelDealCmd.getIsNeedNotice()){
            GameMsg.UserCancelDealResult userCancelDealResult = newBuilder.build();
            ctx.writeAndFlush(userCancelDealResult);
            targetUser.getCtx().writeAndFlush(userCancelDealResult);
        }
    }
}
