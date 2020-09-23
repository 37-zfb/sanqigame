package server.cmdhandler.deal;

import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.UserManager;
import server.cmdhandler.ICmdHandler;
import server.model.Deal;
import server.model.User;
import util.MyUtil;

/**
 * @author 张丰博
 * 重置交易金币
 */
@Component
@Slf4j
public class UserResetMoneyCmdHandler implements ICmdHandler<GameMsg.UserResetMoneyCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserResetMoneyCmd userResetMoneyCmd) {

        MyUtil.checkIsNull(ctx, userResetMoneyCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        Deal deal = user.getDeal();
        if (deal == null || deal.getTargetId() == null || deal.getInitiatorId() == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_DEAL_STATUS);
        }

        if ((deal.getInitiatorId().equals(user.getUserId()) && user.getDeal().isInitiatorIsDetermine()) ||
                (deal.getTargetId().equals(user.getUserId()) && user.getDeal().isTargetIsDetermine())) {
            throw new CustomizeException(CustomizeErrorCode.PROPS_ADD_COMPLETE);
        }

        int money = userResetMoneyCmd.getMoney();
        if (money == 0){
            return;
        }

        if (user.getMoney() < money) {
            throw new CustomizeException(CustomizeErrorCode.USER_MONEY_INSUFFICIENT);
        }

        // 金币
        if (user.getUserId() == deal.getInitiatorId()) {
            deal.setInitiatorMoney(money);
        }

        if (user.getUserId() == deal.getTargetId()) {
            deal.setTargetMoney(money);
        }
        log.info("用户: {} 重置 {} 金币;", user.getUserName(), money);


        GameMsg.UserResetMoneyResult userResetMoneyResult = GameMsg.UserResetMoneyResult.newBuilder()
                .setMoney(money)
                .build();
        Integer targetId = null;
        if (user.getUserId() == deal.getInitiatorId()) {
            targetId = deal.getTargetId();
        }
        if (user.getUserId() == deal.getTargetId()) {
            targetId = deal.getInitiatorId();
        }

        User targetUser = UserManager.getUserById(targetId);
        targetUser.getCtx().writeAndFlush(userResetMoneyResult);

    }
}
