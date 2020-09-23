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
import server.model.PlayDeal;
import server.model.User;
import server.UserManager;
import util.MyUtil;

/**
 * @author 张丰博
 * 玩家添加道具完毕
 */
@Component
@Slf4j
public class UserAddCompleteCmdHandler implements ICmdHandler<GameMsg.UserAddCompleteCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserAddCompleteCmd userCancelDealCmd) {

        MyUtil.checkIsNull(ctx, userCancelDealCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        Deal deal = user.getDeal();
        if (deal == null || deal.getTargetId() == null || deal.getInitiatorId() == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_DEAL_STATUS);
        }


        User targetUser = null;
        if (user.getUserId() == deal.getInitiatorId()) {
             targetUser = UserManager.getUserById(deal.getTargetId());
        }
        if (user.getUserId() == deal.getTargetId()) {
            targetUser = UserManager.getUserById(deal.getInitiatorId());
        }

        if (targetUser == null){
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_EXISTS);
        }

        if (user.getUserId() == deal.getInitiatorId()) {
            deal.setInitiatorIsDetermine(true);
        }
        if (user.getUserId() == deal.getTargetId()) {
            deal.setTargetIsDetermine(true);
        }

        log.info("用户 {} 添加道具完毕;", user.getUserName());

        GameMsg.UserInfo.Builder userInfo = GameMsg.UserInfo.newBuilder()
                .setUserId(user.getUserId())
                .setUserName(user.getUserName());
        GameMsg.UserAddCompleteResult userAddCompleteResult = GameMsg.UserAddCompleteResult.newBuilder()
                .setUserInfo(userInfo)
                .build();
        targetUser.getCtx().writeAndFlush(userAddCompleteResult);

    }
}
