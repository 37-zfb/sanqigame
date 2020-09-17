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

        if (user.getPLAY_DEAL().getTargetUserId() != 0) {
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
            // 同意交易， 发起者用户
            user.getPLAY_DEAL().setTargetUserId(originateId);

            //修改交易状态成功
            log.info("用户 {} 同意 {} 交易;", user.getUserName(), originateUser.getUserName());

            Object monitor = new Object();
            user.getPLAY_DEAL().setCompleteDealMonitor(monitor);

            GameMsg.UserDealRequestResult userDealRequestResult = newBuilder
                    .setIsAgree(true)
                    .setIsSuccess(true)
                    .setTargetUserId(user.getUserId())
                    .setTargetUserName(user.getUserName())
                    .build();
            ctx.writeAndFlush(userDealRequestResult);
            originateUser.getCtx().writeAndFlush(userDealRequestResult);

//                // 修改交易状态失败
//                originateUser.getPLAY_DEAL().getTargetUserId()
//                        .compareAndSet(user.getUserId(), 0);
//                user.getPLAY_DEAL().getTargetUserId()
//                        .compareAndSet(originateId, 0);
//
//                log.info("用户 {}、{} 交易失败;", user.getUserName(), originateUser.getUserName());
//                GameMsg.UserDealRequestResult userDealRequestResult = newBuilder
//                        .setIsSuccess(false)
//                        .setTargetUserId(user.getUserId())
//                        .build();
//                ctx.writeAndFlush(userDealRequestResult);

        }

    }
}
