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
 * 玩家添加道具完毕
 */
@Component
@Slf4j
public class UserAddCompleteCmdHandler implements ICmdHandler<GameMsg.UserAddCompleteCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserAddCompleteCmd userCancelDealCmd) {

        MyUtil.checkIsNull(ctx, userCancelDealCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayDeal playDeal = user.getPLAY_DEAL();
        int targetId = playDeal.getTargetUserId();
        if (targetId == 0) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_DEAL_STATUS);
        }

        User targetUser = UserManager.getUserById(targetId);
        if (targetUser == null){
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_EXISTS);
        }

        playDeal.setDetermine(true);

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
