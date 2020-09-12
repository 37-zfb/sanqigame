package server.cmdhandler.team;

import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.cmdhandler.task.listener.TaskPublicMethod;
import server.model.User;
import server.model.UserManager;
import util.MyUtil;

/**
 * @author 张丰博
 * 对方的应答结果
 */
@Component
@Slf4j
public class UserJoinTeamCmdHandler implements ICmdHandler<GameMsg.UserJoinTeamCmd> {

    @Autowired
    private TaskPublicMethod taskPublicMethod;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserJoinTeamCmd userJoinTeamCmd) {
        MyUtil.checkIsNull(ctx, userJoinTeamCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        boolean isJoin = userJoinTeamCmd.getIsJoin();
        int originateOrTargetUserId = userJoinTeamCmd.getOriginateUserId();
        // 发起者用户
        User originateOrTargetUser = UserManager.getUserById(originateOrTargetUserId);
        if (originateOrTargetUser == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_EXISTS);
        }

        if (!originateOrTargetUser.getInvitationUserId().contains(user.getUserId())) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_INVITE);
        }


        if (user.getPlayTeam() != null) {
            TeamUtil.getTeamUtil().originateHaveTeam(originateOrTargetUser, user);
            return;
        }


        GameMsg.UserJoinTeamResult.Builder newBuilder = GameMsg.UserJoinTeamResult.newBuilder();
        if (!isJoin) {
            // 此时不加入队伍，
            GameMsg.UserJoinTeamResult userJoinTeamResult = newBuilder
                    .setIsJoin(false)
                    .setTargetName(user.getUserName())
                    .build();
            originateOrTargetUser.getCtx().writeAndFlush(userJoinTeamResult);
            log.info("{} 拒绝了 {} 的组队邀请;", user.getUserName(), originateOrTargetUser.getUserName());
        } else {
            // 此时加入队伍，
            GameMsg.UserJoinTeamResult userJoinTeamResult = newBuilder
                    .setIsJoin(true)
                    .setTargetId(user.getUserId())
                    .setTargetName(user.getUserName())
                    .build();
            originateOrTargetUser.getCtx().writeAndFlush(userJoinTeamResult);
            log.info("{} 同意了 {} 的组队邀请;", user.getUserName(), originateOrTargetUser.getUserName());

            taskPublicMethod.listener(user);
        }

    }


}



