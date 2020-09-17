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
import server.UserManager;
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

        PlayDeal playDeal = user.getPLAY_DEAL();
        int targetId = playDeal.getTargetUserId();
        if (targetId == 0) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_DEAL_STATUS);
        }

        GameMsg.UserCancelDealResult.Builder newBuilder = GameMsg.UserCancelDealResult.newBuilder();

        playDeal.setTargetUserId(0);

        // 成功
        playDeal.getPrepareProps().clear();
        playDeal.setPrepareMoney(0);
        playDeal.setCompleteDealMonitor(null);
        newBuilder.setIsSuccess(true).setUserId(user.getUserId());
        log.info("用户 {} 取消交易;", user.getUserName());


        if (!userCancelDealCmd.getIsNeedNotice()) {
            return;
        }

        GameMsg.UserCancelDealResult userCancelDealResult = newBuilder.build();
        ctx.writeAndFlush(userCancelDealResult);

        User targetUser = UserManager.getUserById(targetId);
        if (targetUser == null) {
            return;
        }
        targetUser.getCtx().writeAndFlush(userCancelDealResult);
    }
}
