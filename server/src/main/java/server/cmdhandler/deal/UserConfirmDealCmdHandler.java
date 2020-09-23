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
import server.model.DealProps;
import server.model.PlayDeal;
import server.model.User;
import server.UserManager;
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

        if (!deal.isInitiatorIsDetermine() || !deal.isTargetIsDetermine()) {
            throw new CustomizeException(CustomizeErrorCode.TARGET_NOT_COMPLETE);
        }



        synchronized (user.getDeal()) {
            deal.setAgreeNumber(deal.getAgreeNumber()+1);

            GameMsg.UserConfirmDealResult.Builder newBuilder = GameMsg.UserConfirmDealResult.newBuilder();
            GameMsg.UserConfirmDealResult userConfirmDealResult;
            if (deal.getAgreeNumber() == 2) {
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
