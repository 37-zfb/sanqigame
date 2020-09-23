package server.cmdhandler.deal;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
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
 * 修改交易状态
 */
@Component
@Slf4j
public class UserModifyDealStateCmdHandler implements ICmdHandler<GameMsg.UserModifyDealStateCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserModifyDealStateCmd userModifyDealStateCmd) {
        MyUtil.checkIsNull(ctx, userModifyDealStateCmd);

        int targetId = userModifyDealStateCmd.getTargetId();
        User targetUser = UserManager.getUserById(targetId);
        if (targetUser == null) {
            return;
        }

        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        User user = UserManager.getUserById(userId);
        if (user == null) {
            //离线
            sendFail(targetUser);
            return;
        }

        if (!user.getDEAL_ID_SET().contains(targetId)) {
            //不包含
            sendFail(targetUser);
            return;
        }

        user.setDeal(targetUser.getDeal());
        user.getDeal().setInitiatorId(userId);

        log.info("用户 {} 和 {} 交易通道建立成功;", user.getUserName(),targetUser.getUserName());

        GameMsg.UserModifyDealStateResult userModifyDealStateResult = GameMsg.UserModifyDealStateResult
                .newBuilder()
                .setIsSuccess(true)
                .build();
        ctx.writeAndFlush(userModifyDealStateResult);
    }

    private void sendFail(User targetUser) {
        GameMsg.UserModifyDealStateResult userModifyDealStateResult = GameMsg.UserModifyDealStateResult
                .newBuilder()
                .setIsSuccess(false)
                .build();
        targetUser.getCtx().writeAndFlush(userModifyDealStateResult);
    }

}
