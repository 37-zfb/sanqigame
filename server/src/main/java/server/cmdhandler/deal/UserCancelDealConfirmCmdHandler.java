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
 * 取消确认消息处理类
 */
@Component
@Slf4j
public class UserCancelDealConfirmCmdHandler implements ICmdHandler<GameMsg.UserCancelDealConfirmCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserCancelDealConfirmCmd userCancelDealConfirmCmd) {

        MyUtil.checkIsNull(ctx, userCancelDealConfirmCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayDeal playDeal = user.getPLAY_DEAL();
        int targetId = playDeal.getTargetUserId().get();
        if (targetId == 0) {
            // 此时不在交易状态
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_DEAL_STATUS);
        }

        playDeal.setDetermine(false);
        playDeal.setAgreeNumber(0);
        playDeal.setReceiveMoney(0);
        playDeal.getReceiveProps().clear();

        log.info("用户 {} 取消确认;", user.getUserName());



        if (!userCancelDealConfirmCmd.getIsNeedNotice()) {
            return;
        }

        User targetUser = UserManager.getUserById(targetId);
        if (targetUser == null){
            return;
        }
        GameMsg.UserCancelDealConfirmResult userCancelDealConfirmResult =
                GameMsg.UserCancelDealConfirmResult
                        .newBuilder()
                        .build();
        targetUser.getCtx().writeAndFlush(userCancelDealConfirmResult);

    }
}
