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
 * 目标用户响应结果
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

        if (originateUser == null){
            throw new CustomizeException(CustomizeErrorCode.ORIGINATE_USER_NOT_FOUNT);
        }
        if (!originateUser.getPLAY_DEAL().getUserIdSet().contains(dealTargetUser.getUserId())){
            throw new CustomizeException(CustomizeErrorCode.ORIGINATE_USER_NOT_REQUEST);
        }

        GameMsg.UserDealRequestResult.Builder newBuilder = GameMsg.UserDealRequestResult.newBuilder();
        //移除发起用户集合中的id
        originateUser.getPLAY_DEAL().getUserIdSet().remove(dealTargetUser.getUserId());
        if (!isAgree) {
            // 拒绝交易,
            log.info("用户 {} 拒绝 {} 交易;", dealTargetUser.getUserName(),originateUser.getUserName());
            GameMsg.UserDealRequestResult userDealRequestResult = newBuilder.setIsAgree(false)
                    .setTargetUserId(dealTargetUser.getUserId())
                    .setTargetUserName(dealTargetUser.getUserName())
                    .build();

            ctx.writeAndFlush(userDealRequestResult);
            originateUser.getCtx().writeAndFlush(userDealRequestResult);
        } else {
            // 同意交易， 发起者用户
            boolean isSuccessOriginate = originateUser.getPLAY_DEAL().getTargetUserId().compareAndSet(0, dealTargetUser.getUserId());
            boolean isSuccessTarget = dealTargetUser.getPLAY_DEAL().getTargetUserId().compareAndSet(0, originateId);

            newBuilder.setIsAgree(true);
            if (isSuccessOriginate && isSuccessTarget) {
                log.info("用户 {} 同意 {} 交易;", dealTargetUser.getUserName(),originateUser.getUserName());
                // 成功
                GameMsg.UserDealRequestResult userDealRequestResult = newBuilder
                        .setIsSuccess(true)
                        .setTargetUserId(dealTargetUser.getUserId())
                        .setTargetUserName(dealTargetUser.getUserName())
                        .build();
                ctx.writeAndFlush(userDealRequestResult);
                originateUser.getCtx().writeAndFlush(userDealRequestResult);
            } else {
                // 失败,对方已经在交易状态了
                log.info("用户 {}、{} 交易失败;", dealTargetUser.getUserName(),originateUser.getUserName());
                GameMsg.UserDealRequestResult userDealRequestResult = newBuilder
                        .setIsSuccess(false)
                        .setTargetUserId(dealTargetUser.getUserId())
                        .build();
                ctx.writeAndFlush(userDealRequestResult);
            }
        }

    }
}
