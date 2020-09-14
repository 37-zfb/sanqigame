package server.cmdhandler.team;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.cmdhandler.task.listener.TaskPublicMethod;
import server.model.PlayTeam;
import server.model.User;
import server.model.UserManager;
import util.MyUtil;

/**
 * @author 张丰博
 * 同意队伍请求的被邀请方，加入队伍
 */
@Component
@Slf4j
public class UserJoinTeamPerformCmdHandler implements ICmdHandler<GameMsg.UserJoinTeamPerformCmd> {
    @Autowired
    private TaskPublicMethod taskPublicMethod;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserJoinTeamPerformCmd userJoinTeamPerform) {

        MyUtil.checkIsNull(ctx, userJoinTeamPerform);
        User originateUser = PublicMethod.getInstance().getUser(ctx);

        int targetId = userJoinTeamPerform.getTargetId();

        boolean isAgree = userJoinTeamPerform.getIsAgree();
        if (!isAgree) {
            //拒绝加入
            originateUser.getInvitationUserId().remove(targetId);
            return;
        }

        User targetUser = UserManager.getUserById(targetId);
        if (targetUser == null) {
            return;
        }

        PlayTeam originateUserPlayTeam = originateUser.getPlayTeam();
        PlayTeam targetUserPlayTeam = targetUser.getPlayTeam();

        if (!originateUser.getInvitationUserId().contains(targetUser.getUserId())) {
            //此时不在邀请人中
            return;
        }


        if (originateUserPlayTeam == null && targetUserPlayTeam == null) {
            // 两者都不是组队状态
            bothNoTeam(targetUser, originateUser);
        } else if (originateUserPlayTeam != null && targetUserPlayTeam != null) {
            //两者都有队伍
            TeamUtil.getTeamUtil().enterTeamFail(ctx);
        } else if (originateUserPlayTeam != null) {
            // 发起者有队伍，应答者没有队伍
            TeamUtil.getTeamUtil().originateHaveTeam(targetUser, originateUser);
        } else {
            // 加入对方的队伍
            TeamUtil.getTeamUtil().enterTeamFail(ctx);
        }

        originateUser.getInvitationUserId().remove(targetId);
    }




    /**
     * 两者都没有队伍
     *
     * @param targetUser
     * @param originateUser
     */
    private void bothNoTeam(User targetUser, User originateUser) {
        GameMsg.UserJoinTeamPerformResult.Builder newBuilder = GameMsg.UserJoinTeamPerformResult.newBuilder();
        synchronized (targetUser.getTEAM_MONITOR()) {
            // 使用的被邀请者的 锁
            if (originateUser.getPlayTeam() == null && targetUser.getPlayTeam() == null) {
                // 发起组队的用户
                PlayTeam playTeam = new PlayTeam();
                playTeam.setTeamLeaderId(originateUser.getUserId());
                Integer[] teamMember = playTeam.getTEAM_MEMBER();
                teamMember[0] = originateUser.getUserId();
                teamMember[1] = targetUser.getUserId();
                originateUser.setPlayTeam(playTeam);


                targetUser.setPlayTeam(playTeam);

                playTeam.setTeamNumber(2);

                // 通知发起者，
                GameMsg.UserInfo userInfo = GameMsg.UserInfo.newBuilder()
                        .setUserId(targetUser.getUserId())
                        .setUserName(targetUser.getUserName())
                        .build();
                GameMsg.UserJoinTeamPerformResult userJoinTeamResult = newBuilder
                        .addUserInfo(userInfo)
                        .setTeamLeaderId(playTeam.getTeamLeaderId())
                        .setIsJoin(true)
                        .build();
                originateUser.getCtx().writeAndFlush(userJoinTeamResult);

                // 通知被发起者
                GameMsg.UserInfo originateUserInfo = GameMsg.UserInfo.newBuilder()
                        .setUserId(originateUser.getUserId())
                        .setUserName(originateUser.getUserName())
                        .build();
                GameMsg.UserJoinTeamPerformResult userJoinTeamResult1 = newBuilder
                        .clearUserInfo()
                        .addUserInfo(originateUserInfo)
                        .setTeamLeaderId(playTeam.getTeamLeaderId())
                        .setIsJoin(true)
                        .build();
                targetUser.getCtx().writeAndFlush(userJoinTeamResult1);
                log.info("用户 {} 加入 {} 的队伍;", targetUser.getUserName(), originateUser.getUserName());

                taskPublicMethod.listener(targetUser);
                taskPublicMethod.listener(originateUser);
            } else {
                // 失败
                log.info("用户 {} 加入 {} 队伍 失败;", targetUser.getUserName(), originateUser.getUserName());
                TeamUtil.getTeamUtil().enterTeamFail(targetUser.getCtx());
            }
        }
    }



}
