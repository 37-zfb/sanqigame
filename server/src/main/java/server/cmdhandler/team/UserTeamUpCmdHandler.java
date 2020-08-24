package server.cmdhandler.team;

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
 * 用户发起组队邀请
 */
@Slf4j
@Component
public class UserTeamUpCmdHandler implements ICmdHandler<GameMsg.UserTeamUpCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserTeamUpCmd userTeamUpCmd) {
        MyUtil.checkIsNull(ctx, userTeamUpCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        int targetUserId = userTeamUpCmd.getTargetUserId();
        User targetUser = UserManager.getUserById(targetUserId);

        user.getInvitationUserId().add(targetUserId);


        log.info("用户: {} 发起组队, 询问: {}", user.getUserName(), targetUser.getUserName());
        GameMsg.AskTeamUpResult build = GameMsg.AskTeamUpResult.newBuilder()
                .setOriginateUserId(user.getUserId())
                .setOriginateUserName(user.getUserName())
                .build();
        targetUser.getCtx().writeAndFlush(build);

    }

}
