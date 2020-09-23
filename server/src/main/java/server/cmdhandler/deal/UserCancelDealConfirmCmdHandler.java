package server.cmdhandler.deal;

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
 * 取消确认消息处理类
 */
@Component
@Slf4j
public class UserCancelDealConfirmCmdHandler implements ICmdHandler<GameMsg.UserCancelDealConfirmCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserCancelDealConfirmCmd userCancelDealConfirmCmd) {

        MyUtil.checkIsNull(ctx, userCancelDealConfirmCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        Deal deal = user.getDeal();
        if (deal == null || deal.getInitiatorId() == null || deal.getTargetId() == null ) {
            return;
        }

        if (!deal.isInitiatorIsDetermine() && !deal.isTargetIsDetermine()){
            //此时还没有 确认交易 状态
            return;
        }

        Integer targetId = 0;
        if (user.getUserId() == deal.getInitiatorId()) {
            deal.setInitiatorIsDetermine(false);
            targetId = deal.getTargetId();
        }
        if (user.getUserId() == deal.getTargetId()) {
            deal.setTargetIsDetermine(false);
            targetId = deal.getInitiatorId();
        }
        deal.setAgreeNumber(0);

        log.info("用户 {} 取消确认;", user.getUserName());



        User targetUser = UserManager.getUserById(targetId);
        if (targetUser == null){
            return;
        }

        if (!deal.isInitiatorIsDetermine() && !deal.isTargetIsDetermine()){
            return;
        }
        GameMsg.UserCancelDealConfirmResult userCancelDealConfirmResult =
                GameMsg.UserCancelDealConfirmResult
                        .newBuilder()
                        .build();
        targetUser.getCtx().writeAndFlush(userCancelDealConfirmResult);
    }
}
