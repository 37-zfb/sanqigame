package server.cmdhandler.arena;

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
 */
@Component
@Slf4j
public class UserChooseOpponentCmdHandler implements ICmdHandler<GameMsg.UserChooseOpponentCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserChooseOpponentCmd userChooseOpponentCmd) {

        MyUtil.checkIsNull(ctx, userChooseOpponentCmd);

        User user = PublicMethod.getInstance().getUser(ctx);
        int userId = userChooseOpponentCmd.getUserId();
        // 要挑战的目标
        User targetUser = UserManager.getUserById(userId);

        log.info("玩家: {},向玩家: {} 发起了挑战;", user.getUserName(),targetUser.getUserName());

        // 询问目标用户是否接受挑战
        GameMsg.TargetUserChallengeResult targetUserChallengeResult =
                GameMsg.TargetUserChallengeResult.newBuilder().setOriginateUserId(user.getUserId()).build();
        targetUser.getCtx().writeAndFlush(targetUserChallengeResult);
    }
}
