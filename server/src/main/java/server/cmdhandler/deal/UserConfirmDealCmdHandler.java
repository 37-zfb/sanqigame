package server.cmdhandler.deal;

import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.DealProps;
import server.model.PlayDeal;
import server.model.User;
import server.model.UserManager;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 * 确定交易消息处理类
 */
@Component
@Slf4j
public class UserConfirmDealCmdHandler implements ICmdHandler<GameMsg.UserConfirmDealCmd> {

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserConfirmDealCmd userConfirmDealCmd) {

        MyUtil.checkIsNull(ctx, userConfirmDealCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayDeal playDeal = user.getPLAY_DEAL();
        int targetId = playDeal.getTargetUserId();
        if (targetId == 0) {
            // 此时不在交易状态
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_DEAL_STATUS);
        }

        User targetUser = UserManager.getUserById(targetId);
        if (targetUser == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_EXISTS);
        }

        if (!playDeal.isDetermine() || !targetUser.getPLAY_DEAL().isDetermine()) {
            throw new CustomizeException(CustomizeErrorCode.TARGET_NOT_COMPLETE);
        }

        Integer prepareMoney = playDeal.getPrepareMoney();
        Map<Integer, DealProps> prepareProps = playDeal.getPrepareProps();

        targetUser.getPLAY_DEAL().setReceiveMoney(prepareMoney);
        targetUser.getPLAY_DEAL().getReceiveProps().putAll(prepareProps);


        synchronized (playDeal.getCompleteDealMonitor()) {
            playDeal.setAgreeNumber(playDeal.getAgreeNumber() + 1);
            targetUser.getPLAY_DEAL().setAgreeNumber(targetUser.getPLAY_DEAL().getAgreeNumber() + 1);

            GameMsg.UserConfirmDealResult.Builder newBuilder = GameMsg.UserConfirmDealResult.newBuilder();
            GameMsg.UserConfirmDealResult userConfirmDealResult;
            if (playDeal.getAgreeNumber() == 2) {
                //此时两个都同意了
                userConfirmDealResult = newBuilder
                        .setIsSuccess(true)
                        .build();
                ctx.writeAndFlush(userConfirmDealResult);
            } else {
                //此时只有一个同意
                userConfirmDealResult = newBuilder
                        .setIsSuccess(false)
                        .build();
            }
            log.info("用户 {} 确定交易;", user.getUserName());
            targetUser.getCtx().writeAndFlush(userConfirmDealResult);
        }


    }
}
