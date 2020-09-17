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

import java.util.Map;

/**
 * @author 张丰博
 * 玩家向另一个玩家发送交易请求
 */
@Component
@Slf4j
public class UserDealRequestCmdHandler implements ICmdHandler<GameMsg.UserDealRequestCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserDealRequestCmd userDealRequestCmd) {
        MyUtil.checkIsNull(ctx, userDealRequestCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        if (user.getPLAY_DEAL().getTargetUserId() != 0) {
            throw new CustomizeException(CustomizeErrorCode.DEAL_STATE);
        }

        int targetUserId = userDealRequestCmd.getUserId();
        if (targetUserId == user.getUserId()) {
            throw new CustomizeException(CustomizeErrorCode.DEAL_REQUEST_ERROR);
        }

        User targetUser = UserManager.getUserById(targetUserId);
        if (targetUser == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_EXISTS);
        }


        user.getPLAY_DEAL().getUserIdSet().add(targetUserId);

        log.info("用户: {} ,请求 {} 交易;", user.getUserName(), targetUser.getUserName());
        GameMsg.AskTargetUserResult askTargetUserResult = GameMsg.AskTargetUserResult.newBuilder()
                .setOriginateId(user.getUserId())
                .setOriginateName(user.getUserName())
                .build();

        targetUser.getCtx().writeAndFlush(askTargetUserResult);
    }


}
