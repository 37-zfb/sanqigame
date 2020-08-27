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
 * 被邀请者的应答结果
 */
@Component
@Slf4j
public class UserJoinTeamCmdHandler implements ICmdHandler<GameMsg.UserJoinTeamCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserJoinTeamCmd userJoinTeamCmd) {
        MyUtil.checkIsNull(ctx, userJoinTeamCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        boolean isJoin = userJoinTeamCmd.getIsJoin();
        int originateUserId = userJoinTeamCmd.getOriginateUserId();
        // 发起者用户
        User originateUser = UserManager.getUserById(originateUserId);

        GameMsg.UserJoinTeamResult.Builder newBuilder = GameMsg.UserJoinTeamResult.newBuilder();

        if (!isJoin) {
            // 此时不加入队伍，
            originateUser.getInvitationUserId().remove(user.getUserId());

            GameMsg.UserJoinTeamResult userJoinTeamResult = newBuilder.setIsJoin(false).build();
            originateUser.getCtx().writeAndFlush(userJoinTeamResult);
            log.info("{} 拒绝了 {} 的组队邀请;", user.getUserName(), originateUser.getUserName());
        } else {
            // 此时加入队伍，
            GameMsg.UserJoinTeamResult userJoinTeamResult = newBuilder.setIsJoin(true).setTargetId(user.getUserId()).build();
            originateUser.getCtx().writeAndFlush(userJoinTeamResult);
            log.info("{} 同意了 {} 的组队邀请;", user.getUserName(), originateUser.getUserName());
        }



    }





}



