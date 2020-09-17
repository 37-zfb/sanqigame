package server.cmdhandler.team;

import constant.TeamConst;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.cmdhandler.task.listener.TaskUtil;
import server.model.User;
import server.UserManager;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 * 对方的应答结果
 */
@Component
@Slf4j
public class UserJoinTeamCmdHandler implements ICmdHandler<GameMsg.UserJoinTeamCmd> {

    @Autowired
    private TaskUtil taskUtil;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserJoinTeamCmd userJoinTeamCmd) {
        MyUtil.checkIsNull(ctx, userJoinTeamCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        boolean isJoin = userJoinTeamCmd.getIsJoin();
        int originateId = userJoinTeamCmd.getOriginateUserId();
        // 发起者用户
        User originateUser = UserManager.getUserById(originateId);
        if (originateUser == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_EXISTS);
        }

        Map<Integer, Long> invitationUserIdMap = originateUser.getInvitationUserIdMap();
        if (invitationUserIdMap.get(user.getUserId()) == null ||
                invitationUserIdMap.get(user.getUserId()) - System.currentTimeMillis() > TeamConst.INVITATION_TIMEOUT) {
            //此时没有邀请，或邀请已过期
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_INVITE);
        }

        if (isJoin) {
            TeamUtil teamUtil = TeamUtil.getTeamUtil();

            if (user.getPlayTeam() != null) {
                //被邀请者有队伍,先退出队伍
                teamUtil.quitTeam(user);
            }

            //被邀请者没有队伍
            teamUtil.joinTeam(user, originateUser);

            taskUtil.listener(user);

        } else {
            GameMsg.UserJoinTeamResult.Builder newBuilder = GameMsg.UserJoinTeamResult.newBuilder();
            GameMsg.UserJoinTeamResult userJoinTeamResult = newBuilder
                    .setIsJoin(false)
                    .setTargetName(user.getUserName())
                    .build();
            originateUser.getCtx().writeAndFlush(userJoinTeamResult);
            log.info("{} 拒绝了 {} 的组队邀请;", user.getUserName(), originateUser.getUserName());
        }

    }


}



