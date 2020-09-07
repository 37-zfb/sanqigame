package server.cmdhandler.team;

import constant.TeamConst;
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
public class UserJoinTeamPerformHandler implements ICmdHandler<GameMsg.UserJoinTeamPerformCmd> {
    @Autowired
    private TaskPublicMethod taskPublicMethod;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserJoinTeamPerformCmd userJoinTeamPerform) {

        MyUtil.checkIsNull(ctx, userJoinTeamPerform);
        User originateUser = PublicMethod.getInstance().getUser(ctx);

        int targetId = userJoinTeamPerform.getTargetId();
        User user = UserManager.getUserById(targetId);

        PlayTeam originateUserPlayTeam = originateUser.getPlayTeam();
        PlayTeam userPlayTeam = user.getPlayTeam();

        if (!originateUser.getInvitationUserId().contains(user.getUserId())) {
            //此时不在邀请人中
            return;
        }

        // 此时两者都不是组队状态
        if (originateUserPlayTeam == null && userPlayTeam == null) {
            // 下面的逻辑，两者都不是组队状态，使用目标用户的锁，
            bothNoTeam(user, originateUser);

        } else if (originateUserPlayTeam != null && userPlayTeam != null) {
            // 两者都有队伍，此时不能组队
            enterTeamFail(ctx);
        } else if (originateUserPlayTeam != null) {
            // 发起者有队伍，应答者没有队伍
            originateHaveTeam(user, originateUser);

        } else {
            // 发起者没有队伍，应答者有队伍
            enterTeamFail(ctx);
        }
        // 移除被邀请者
        user.getInvitationUserId().remove(targetId);
    }


    private void enterTeamFail(ChannelHandlerContext ctx) {
        GameMsg.UserEnterTeamFailResult userEnterTeamFailResult = GameMsg.UserEnterTeamFailResult.newBuilder().build();
        ctx.writeAndFlush(userEnterTeamFailResult);
    }

    /**
     * 两者都没有队伍
     *
     * @param user
     * @param originateUser
     */
    private void bothNoTeam(User user, User originateUser) {
        GameMsg.UserJoinTeamPerformResult.Builder newBuilder = GameMsg.UserJoinTeamPerformResult.newBuilder();
        synchronized (user.getTEAM_MONITOR()) {
            // 使用的被邀请者的 锁
            if (originateUser.getPlayTeam() == null && user.getPlayTeam() == null) {
                // 发起组队的用户
                PlayTeam playTeam = new PlayTeam();
                playTeam.setTeamLeaderId(originateUser.getUserId());
                Integer[] team_member = playTeam.getTEAM_MEMBER();
                team_member[0] = originateUser.getUserId();
                team_member[1] = user.getUserId();
                originateUser.setPlayTeam(playTeam);

                // 给自己的 playTeam 赋值
                user.setPlayTeam(playTeam);

                playTeam.setTeamNumber(2);

                // 通知发起者，
                GameMsg.UserInfo userInfo = GameMsg.UserInfo.newBuilder()
                        .setUserId(user.getUserId())
                        .setUserName(user.getUserName())
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
                user.getCtx().writeAndFlush(userJoinTeamResult1);
                log.info("用户 {} 加入 {} 的队伍;", user.getUserName(), originateUser.getUserName());

                taskPublicMethod.listener(user);
                taskPublicMethod.listener(originateUser);
            } else if (originateUser.getPlayTeam() != null && user.getPlayTeam() == null) {

            } else {
                // 失败
                log.info("用户 {} 加入 {} 的队伍 失败;", user.getUserName(), originateUser.getUserName());
                enterTeamFail(user.getCtx());
            }
        }
    }

    /**
     * 发起者有队伍，应答者没有队伍
     *
     * @param user
     * @param originateUser
     */
    private void originateHaveTeam(User user, User originateUser) {
        GameMsg.UserJoinTeamPerformResult.Builder newBuilder = GameMsg.UserJoinTeamPerformResult.newBuilder();
        //发起者有队伍，
        synchronized (user.getTEAM_MONITOR()) {
            if (user.getPlayTeam() == null && originateUser.getPlayTeam() != null && originateUser.getPlayTeam().getTeamNumber() <= TeamConst.MAX_NUMBER) {
                user.setPlayTeam(originateUser.getPlayTeam());
                //
                Integer[] team_member = user.getPlayTeam().getTEAM_MEMBER();

                // 先通知队伍成员
                GameMsg.UserInfo userInfo = GameMsg.UserInfo.newBuilder()
                        .setUserName(user.getUserName())
                        .setUserId(user.getUserId())
                        .build();
                newBuilder.addUserInfo(userInfo).setIsJoin(true);
                GameMsg.UserJoinTeamPerformResult userJoinTeamResult1 = newBuilder.build();
                for (Integer id : team_member) {
                    if (id != null) {
                        User teamMember = UserManager.getUserById(id);
                        teamMember.getCtx().writeAndFlush(userJoinTeamResult1);
                    }
                }

                // 清空builder中的用户信息
                newBuilder.clearUserInfo();
                for (Integer id : team_member) {
                    if (id != null) {
                        User teamMember = UserManager.getUserById(id);
                        GameMsg.UserInfo.Builder userInfo1 = GameMsg.UserInfo.newBuilder()
                                .setUserId(teamMember.getUserId())
                                .setUserName(teamMember.getUserName());
                        if (id.equals(originateUser.getPlayTeam().getTeamLeaderId())) {
                            newBuilder.setTeamLeaderId(id);
                        }
                        newBuilder.addUserInfo(userInfo1);
                    }
                }
                for (int i = 0; i < team_member.length; i++) {
                    if (team_member[i] == null) {
                        team_member[i] = user.getUserId();
                        break;
                    }
                }
                originateUser.getPlayTeam().setTeamNumber(originateUser.getPlayTeam().getTeamNumber() + 1);
                log.info("用户 {} 加入 {} 的队伍;", user.getUserName(), originateUser.getUserName());

                taskPublicMethod.listener(user);

                GameMsg.UserJoinTeamPerformResult userJoinTeamResult = newBuilder.setIsJoin(true).build();
                user.getCtx().writeAndFlush(userJoinTeamResult);
            } else {
                log.info("用户 {} 加入 {} 的队伍失败;", user.getUserName(), originateUser.getUserName());
                enterTeamFail(user.getCtx());
            }
        }

    }

}
