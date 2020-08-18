package server.cmdhandler.team;

import constant.TeamConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayTeam;
import server.model.User;
import server.model.UserManager;
import util.MyUtil;

/**
 * @author 张丰博
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

        PlayTeam originateUserPlayTeam = originateUser.getPlayTeam();
        PlayTeam userPlayTeam = user.getPlayTeam();

        if (!isJoin) {
            // 此时不加入队伍，
            GameMsg.UserJoinTeamResult userJoinTeamResult = newBuilder.setIsJoin(false).build();
            originateUser.getCtx().writeAndFlush(userJoinTeamResult);
            ctx.writeAndFlush(userJoinTeamResult);
            log.info("{} 拒绝了 {} 的组队邀请;", user.getUserName(),originateUser.getUserName());
            return;
        }

        // 此时两者都不是组队状态
        if (originateUserPlayTeam == null && userPlayTeam == null) {
            // 下面的逻辑，两者都不是组队状态
            synchronized (user.getTEAM_MONITOR()) {
                synchronized (originateUser.getTEAM_MONITOR()) {
                    if (originateUser.getPlayTeam() == null && user.getPlayTeam() == null) {
                        // 发起组队的用户
                        PlayTeam playTeam = new PlayTeam();
                        playTeam.setTeamLeaderId(originateUserId);
                        Integer[] team_member = playTeam.getTEAM_MEMBER();
                        team_member[0] = originateUserId;
                        team_member[1] = user.getUserId();
                        originateUser.setPlayTeam(playTeam);

                        // 给自己的 playTeam 赋值
                        user.setPlayTeam(playTeam);

                        // 通知发起者，
                        GameMsg.UserInfo userInfo = GameMsg.UserInfo.newBuilder()
                                .setUserId(user.getUserId())
                                .setUserName(user.getUserName())
                                .build();
                        GameMsg.UserJoinTeamResult userJoinTeamResult = newBuilder
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
                        GameMsg.UserJoinTeamResult userJoinTeamResult1 = newBuilder
                                .clearUserInfo()
                                .addUserInfo(originateUserInfo)
                                .setTeamLeaderId(playTeam.getTeamLeaderId())
                                .setIsJoin(true)
                                .build();
                        ctx.writeAndFlush(userJoinTeamResult1);
                        log.info("用户 {} 加入 {} 的队伍;", user.getUserName(), originateUser.getUserName());
                    } else {
                        // 失败
                        log.info("用户 {} 加入 {} 的队伍 失败;", user.getUserName(), originateUser.getUserName());
                        enterTeamFail(ctx);
                    }
                }
            }
        } else if (originateUserPlayTeam != null && userPlayTeam != null) {
            // 两者都有队伍，此时不能组队
            enterTeamFail(ctx);
            return;
        } else if (userPlayTeam != null) {
            // 发起者没有队伍，此时加入本用户中
            synchronized (originateUser.getTEAM_MONITOR()) {
                synchronized (userPlayTeam.getENTER_TEAM_MONITOR()) {
                    if (userPlayTeam != null && originateUser.getPlayTeam() == null) {
                        originateUser.setPlayTeam(userPlayTeam);
                        //
                        Integer[] team_member = originateUser.getPlayTeam().getTEAM_MEMBER();

                        // 先通知队伍成员
                        GameMsg.UserInfo originateInfo = GameMsg.UserInfo.newBuilder()
                                .setUserName(originateUser.getUserName())
                                .setUserId(originateUser.getUserId())
                                .build();
                        newBuilder.addUserInfo(originateInfo).setIsJoin(true);
                        GameMsg.UserJoinTeamResult userJoinTeamResult1 = newBuilder.build();
                        for (Integer id : team_member) {
                            if (id != null){
                                User teamMember = UserManager.getUserById(id);
                                teamMember.getCtx().writeAndFlush(userJoinTeamResult1);
                            }
                        }

                        // 清空builder中的用户信息
                        newBuilder.clearUserInfo();
                        for (Integer id : team_member) {
                            if (id != null){
                                User teamMember = UserManager.getUserById(id);
                                GameMsg.UserInfo.Builder userInfo = GameMsg.UserInfo.newBuilder()
                                        .setUserId(teamMember.getUserId())
                                        .setUserName(teamMember.getUserName());
                                if (id.equals(userPlayTeam.getTeamLeaderId())) {
                                    newBuilder.setTeamLeaderId(id);
                                }
                                newBuilder.addUserInfo(userInfo);
                            }
                        }
                        for (int i = 0; i < team_member.length; i++) {
                            if (team_member[i] == null) {
                                team_member[i] = originateUserId;
                                break;
                            }
                        }
                        log.info("用户 {} 加入 {} 的队伍;", originateUser.getUserName(), user.getUserName());
                        GameMsg.UserJoinTeamResult userJoinTeamResult = newBuilder.setIsJoin(true).build();
                        originateUser.getCtx().writeAndFlush(userJoinTeamResult);
                    } else {
                        log.info("用户 {} 加入 {} 的队伍失败;", originateUser.getUserName(), user.getUserName());
                        enterTeamFail(ctx);
                    }
                }
            }

        } else {
            // 发起者有队伍，本用户没有加入队伍中
            synchronized (originateUserPlayTeam.getENTER_TEAM_MONITOR()) {
                synchronized (user.getTEAM_MONITOR()) {
                    if (userPlayTeam == null && originateUser.getPlayTeam() != null) {
                        user.setPlayTeam(originateUserPlayTeam);
                        //
                        Integer[] team_member = user.getPlayTeam().getTEAM_MEMBER();

                        // 先通知队伍成员
                        GameMsg.UserInfo userInfo = GameMsg.UserInfo.newBuilder()
                                .setUserName(user.getUserName())
                                .setUserId(user.getUserId())
                                .build();
                        newBuilder.addUserInfo(userInfo).setIsJoin(true);
                        GameMsg.UserJoinTeamResult userJoinTeamResult1 = newBuilder.build();
                        for (Integer id : team_member) {
                            if (id != null){
                                User teamMember = UserManager.getUserById(id);
                                teamMember.getCtx().writeAndFlush(userJoinTeamResult1);
                            }
                        }

                        // 清空builder中的用户信息
                        newBuilder.clearUserInfo();
                        for (Integer id : team_member) {
                            if (id != null){
                                User teamMember = UserManager.getUserById(id);
                                GameMsg.UserInfo.Builder userInfo1 = GameMsg.UserInfo.newBuilder()
                                        .setUserId(teamMember.getUserId())
                                        .setUserName(teamMember.getUserName());
                                if (id.equals(originateUserPlayTeam.getTeamLeaderId())) {
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
                        log.info("用户 {} 加入 {} 的队伍;", user.getUserName(), originateUser.getUserName());
                        GameMsg.UserJoinTeamResult userJoinTeamResult = newBuilder.setIsJoin(true).build();
                        ctx.writeAndFlush(userJoinTeamResult);
                    }else {
                        log.info("用户 {} 加入 {} 的队伍失败;", user.getUserName(), originateUser.getUserName());
                        enterTeamFail(ctx);
                    }
                }
            }

        }

    }


    private void enterTeamFail(ChannelHandlerContext ctx) {
        GameMsg.UserEnterTeamFailResult userEnterTeamFailResult = GameMsg.UserEnterTeamFailResult.newBuilder().build();
        ctx.writeAndFlush(userEnterTeamFailResult);
    }


}



