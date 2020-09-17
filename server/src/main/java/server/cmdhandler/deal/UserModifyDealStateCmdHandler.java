package server.cmdhandler.deal;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
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
 * 修改交易状态
 */
@Component
@Slf4j
public class UserModifyDealStateCmdHandler implements ICmdHandler<GameMsg.UserModifyDealStateCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserModifyDealStateCmd userModifyDealStateCmd) {
        MyUtil.checkIsNull(ctx, userModifyDealStateCmd);

        User user = null;

        int targetId = userModifyDealStateCmd.getTargetId();
        User targetUser = UserManager.getUserById(targetId);
        if (targetUser == null) {
            //目标用户已离线
            user = PublicMethod.getInstance().getUser(ctx);
            user.getPLAY_DEAL().getUserIdSet().remove(targetId);
            return;
        }


        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        if (userId == null) {
            //离线
            sendFail(targetUser);
            return;
        }

        user = UserManager.getUserById(userId);
        if (user == null) {
            //离线
            sendFail(targetUser);
            return;
        }

        if (!user.getPLAY_DEAL().getUserIdSet().contains(targetId)) {
            //不包含
            sendFail(targetUser);
            return;
        }

        user.getPLAY_DEAL().getUserIdSet().remove(targetId);
        user.getPLAY_DEAL().setTargetUserId(targetId);
        user.getPLAY_DEAL().setCompleteDealMonitor(targetUser.getPLAY_DEAL().getCompleteDealMonitor());

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
