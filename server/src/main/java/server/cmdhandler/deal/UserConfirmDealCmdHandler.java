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

    /**
     * 改变用户状态监视器
     */
    private final Object statusMonitor = new Object();

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserConfirmDealCmd userConfirmDealCmd) {

        MyUtil.checkIsNull(ctx, userConfirmDealCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayDeal play_deal = user.getPLAY_DEAL();
        int targetId = play_deal.getTargetUserId().get();
        if (targetId == 0) {
            // 此时不在交易状态
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_DEAL_STATUS);
        }

        Integer prepareMoney = play_deal.getPrepareMoney();
        Map<Integer, DealProps> prepareProps = play_deal.getPrepareProps();

        User targetUser = UserManager.getUserById(targetId);
        targetUser.getPLAY_DEAL().setReceiveMoney(prepareMoney);
        targetUser.getPLAY_DEAL().getReceiveProps().putAll(prepareProps);

        /**
         *  若不加锁： 当a用户给ab都+1后在if前一句线程切换(时间片到期等)此时b又给ab都+1，
         */
        synchronized (statusMonitor) {
            play_deal.setAgreeNumber(play_deal.getAgreeNumber()+1);
            targetUser.getPLAY_DEAL().setAgreeNumber(targetUser.getPLAY_DEAL().getAgreeNumber()+1);

            GameMsg.UserConfirmDealResult.Builder newBuilder = GameMsg.UserConfirmDealResult.newBuilder();
            GameMsg.UserConfirmDealResult userConfirmDealResult;
            if (play_deal.getAgreeNumber() == 2) {
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
