package server.cmdhandler.deal;

import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.UserManager;
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
        User dealTargetUser = PublicMethod.getInstance().getUser(ctx);

        boolean isAgree = dealTargetUserResponseCmd.getIsAgree();
        int originateId = dealTargetUserResponseCmd.getOriginateId();

        User originateUser = UserManager.getUserById(originateId);
        if (originateUser == null) {
            throw new CustomizeException(CustomizeErrorCode.ORIGINATE_USER_NOT_FOUNT);
        }
        if (!originateUser.getPLAY_DEAL().getUserIdSet().contains(dealTargetUser.getUserId())) {
            throw new CustomizeException(CustomizeErrorCode.ORIGINATE_USER_NOT_REQUEST);
        }

        originateUser.getPLAY_DEAL().getUserIdSet().remove(dealTargetUser.getUserId());

        GameMsg.UserDealRequestResult.Builder newBuilder = GameMsg.UserDealRequestResult.newBuilder();
        if (!isAgree) {
            // 拒绝交易,
            log.info("用户 {} 拒绝 {} 交易;", dealTargetUser.getUserName(), originateUser.getUserName());
            GameMsg.UserDealRequestResult userDealRequestResult = newBuilder
                    .setIsAgree(false)
                    .setTargetUserId(dealTargetUser.getUserId())
                    .setTargetUserName(dealTargetUser.getUserName())
                    .build();

            ctx.writeAndFlush(userDealRequestResult);
            originateUser.getCtx().writeAndFlush(userDealRequestResult);
        } else {
            // 同意交易， 发起者用户
            boolean isSuccessOriginate = originateUser.getPLAY_DEAL().getTargetUserId()
                    .compareAndSet(0, dealTargetUser.getUserId());
            boolean isSuccessTarget = dealTargetUser.getPLAY_DEAL().getTargetUserId()
                    .compareAndSet(0, originateId);

            newBuilder.setIsAgree(true);
            if (isSuccessOriginate && isSuccessTarget) {
                //修改交易状态成功
                log.info("用户 {} 同意 {} 交易;", dealTargetUser.getUserName(), originateUser.getUserName());

                Object monitor = new Object();
                dealTargetUser.getPLAY_DEAL().setCompleteDealMonitor(monitor);
                originateUser.getPLAY_DEAL().setCompleteDealMonitor(monitor);

                GameMsg.UserDealRequestResult userDealRequestResult = newBuilder
                        .setIsSuccess(true)
                        .setTargetUserId(dealTargetUser.getUserId())
                        .setTargetUserName(dealTargetUser.getUserName())
                        .build();
                ctx.writeAndFlush(userDealRequestResult);
                originateUser.getCtx().writeAndFlush(userDealRequestResult);
            } else {
                // 修改交易状态失败
                originateUser.getPLAY_DEAL().getTargetUserId()
                        .compareAndSet(dealTargetUser.getUserId(), 0);
                dealTargetUser.getPLAY_DEAL().getTargetUserId()
                        .compareAndSet(originateId, 0);

                log.info("用户 {}、{} 交易失败;", dealTargetUser.getUserName(), originateUser.getUserName());
                GameMsg.UserDealRequestResult userDealRequestResult = newBuilder
                        .setIsSuccess(false)
                        .setTargetUserId(dealTargetUser.getUserId())
                        .build();
                ctx.writeAndFlush(userDealRequestResult);
            }
        }

    }
}
