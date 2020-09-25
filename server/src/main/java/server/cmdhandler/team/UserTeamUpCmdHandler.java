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
import server.model.PlayTeam;
import server.model.User;
import server.UserManager;
import type.TaskType;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 * 用户发起组队邀请
 */
@Slf4j
@Component
public class UserTeamUpCmdHandler implements ICmdHandler<GameMsg.UserTeamUpCmd> {

    @Autowired
    private TaskUtil taskUtil;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserTeamUpCmd userTeamUpCmd) {
        MyUtil.checkIsNull(ctx, userTeamUpCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        int targetUserId = userTeamUpCmd.getTargetUserId();
        User targetUser = UserManager.getUserById(targetUserId);
        if (targetUser == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_EXISTS);
        }

        Map<Integer, Long> invitationUserIdMap = user.getInvitationUserIdMap();
        clearInvitationTimeout(invitationUserIdMap);
        //添加邀请人
        invitationUserIdMap.put(targetUserId, System.currentTimeMillis());

        //创建队伍
        if (user.getPlayTeam() == null) {
            PlayTeam playTeam = new PlayTeam();
            playTeam.setTeamLeaderId(user.getUserId());
            playTeam.setTeamNumber(playTeam.getTeamNumber() + 1);
            playTeam.getTEAM_MEMBER()[0] = user.getUserId();
            user.setPlayTeam(playTeam);

            GameMsg.UserJoinTeamPerformResult userJoinTeamPerformResult = GameMsg.UserJoinTeamPerformResult.newBuilder()
                    .setTeamLeaderId(user.getUserId())
                    .setIsJoin(true)
                    .build();
            ctx.writeAndFlush(userJoinTeamPerformResult);
        }


        log.info("用户: {} 发起组队, 询问: {}", user.getUserName(), targetUser.getUserName());
        GameMsg.AskTeamUpResult build = GameMsg.AskTeamUpResult.newBuilder()
                .setOriginateUserId(user.getUserId())
                .setOriginateUserName(user.getUserName())
                .build();
        targetUser.getCtx().writeAndFlush(build);


        taskUtil.listener(user, TaskType.AddTeamType.getTaskCode());
    }

    private void clearInvitationTimeout(Map<Integer, Long> invitationUserIdMap) {
        if (invitationUserIdMap == null || invitationUserIdMap.isEmpty()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        //大于10秒
        invitationUserIdMap.entrySet().removeIf(next -> currentTime - next.getValue() > TeamConst.INVITATION_TIMEOUT);

    }

}
