package server.cmdhandler.team;

import constant.TeamConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import server.GameServer;
import server.cmdhandler.task.listener.TaskUtil;
import server.model.PlayTeam;
import server.model.User;
import server.UserManager;
import server.model.duplicate.BossMonster;
import server.model.duplicate.Duplicate;
import server.model.profession.SummonMonster;
import type.ProfessionType;

import java.util.Map;

/**
 * @author 张丰博
 */
@Slf4j
public final class TeamUtil {

    private static final TeamUtil TEAM_UTIL = new TeamUtil();

    private TaskUtil taskPublicMethod = GameServer.APPLICATION_CONTEXT.getBean(TaskUtil.class);

    private TeamUtil() {
    }

    public static TeamUtil getTeamUtil() {
        return TEAM_UTIL;
    }

    /**
     * 用户退出队伍
     * @param user
     */
    public void quitTeam(User user) {
        PlayTeam playTeam = user.getPlayTeam();

        if (playTeam == null) {
            return;
        }
        synchronized (playTeam.getTEAM_MONITOR()) {
            Integer[] teamMember = playTeam.getTEAM_MEMBER();
            for (int i = 0; i < teamMember.length; i++) {
                if (teamMember[i] == null || !teamMember[i].equals(user.getUserId())) {
                    continue;
                }

                teamMember[i] = null;
                break;
            }
            playTeam.setTeamNumber(playTeam.getTeamNumber()-1);

            // 如果 队长退出队伍,选择新队员成为队长
            if (playTeam.getTeamLeaderId().equals(user.getUserId())) {
                teamMember = playTeam.getTEAM_MEMBER();
                for (int i = 0; i < teamMember.length; i++) {
                    if (teamMember[i] == null) {
                        continue;
                    }

                    playTeam.setTeamLeaderId(teamMember[i]);
                    break;
                }
            }

            Duplicate currDuplicate = playTeam.getCurrDuplicate();
            if (currDuplicate != null) {
                // 若当前副本不为空
                BossMonster currBossMonster = currDuplicate.getCurrBossMonster();
                synchronized (currBossMonster.getCHOOSE_USER_MONITOR()) {
                    Map<Integer, Integer> userIdMap = currBossMonster.getUserIdMap();
                    userIdMap.remove(user.getUserId());
                }

                if (user.getProfessionId() == ProfessionType.Summoner.getId()) {
                    //如果是召唤师
                    Map<SummonMonster, Integer> summonMonsterMap = currBossMonster.getSummonMonsterMap();
                    Map<Integer, SummonMonster> userSummonMonsterMap = user.getSummonMonsterMap();
                    for (SummonMonster summonMonster : userSummonMonsterMap.values()) {
                        synchronized (currBossMonster.getCHOOSE_USER_MONITOR()) {
                            summonMonsterMap.remove(summonMonster);
                        }
                    }
                    userSummonMonsterMap.clear();
                }

            }

            user.setPlayTeam(null);
            GameMsg.UserInfo.Builder userInfo = GameMsg.UserInfo.newBuilder()
                    .setUserId(user.getUserId())
                    .setUserName(user.getUserName());
            GameMsg.UserQuitTeamResult userQuitTeamResult = GameMsg.UserQuitTeamResult.newBuilder()
                    .setUserInfo(userInfo)
                    .setTeamLeaderId(playTeam.getTeamLeaderId())
                    .build();
            user.getCtx().writeAndFlush(userQuitTeamResult);

            for (Integer id : teamMember) {
                if (id == null) {
                    continue;
                }

                User userById = UserManager.getUserById(id);
                if (userById == null){
                    continue;
                }

                userById.getCtx().writeAndFlush(userQuitTeamResult);
            }
        }

    }


    /**
     * 发起者有队伍，应答者没有队伍
     *
     * @param targetUser 需要加入队伍的人
     * @param teamUser 已加入队伍的用户
     */
    public void joinTeam(User targetUser, User teamUser) {
        GameMsg.UserJoinTeamPerformResult.Builder newBuilder = GameMsg.UserJoinTeamPerformResult.newBuilder();
        //发起者有队伍，
        synchronized (teamUser.getPlayTeam().getTEAM_MONITOR()) {
            if (targetUser.getPlayTeam() == null &&
                    teamUser.getPlayTeam() != null &&
                    teamUser.getPlayTeam().getTeamNumber() < TeamConst.MAX_NUMBER) {

                targetUser.setPlayTeam(teamUser.getPlayTeam());

                Integer[] teamMemberArr = targetUser.getPlayTeam().getTEAM_MEMBER();

                // 先通知队伍成员
                GameMsg.UserInfo userInfo = GameMsg.UserInfo.newBuilder()
                        .setUserName(targetUser.getUserName())
                        .setUserId(targetUser.getUserId())
                        .setCurrHp(targetUser.getCurrHp())
                        .setCurrMp(targetUser.getCurrMp())
                        .build();
                newBuilder.addUserInfo(userInfo).setIsJoin(true);
                GameMsg.UserJoinTeamPerformResult userJoinTeamResult1 = newBuilder.build();
                for (Integer id : teamMemberArr) {
                    if (id != null) {
                        User teamMember = UserManager.getUserById(id);
                        teamMember.getCtx().writeAndFlush(userJoinTeamResult1);
                    }
                }

                // 清空builder中的用户信息
                newBuilder.clearUserInfo();
                for (Integer id : teamMemberArr) {
                    if (id == null) {
                        continue;
                    }

                    User teamMember = UserManager.getUserById(id);
                    if (teamMember == null) {
                        continue;
                    }

                    GameMsg.UserInfo.Builder userInfo1 = GameMsg.UserInfo.newBuilder()
                            .setUserId(teamMember.getUserId())
                            .setUserName(teamMember.getUserName())
                            .setCurrHp(teamMember.getCurrHp())
                            .setCurrMp(teamMember.getCurrMp());
                    if (id.equals(teamUser.getPlayTeam().getTeamLeaderId())) {
                        newBuilder.setTeamLeaderId(id);
                    }
                    newBuilder.addUserInfo(userInfo1);
                }

                for (int i = 0; i < teamMemberArr.length; i++) {
                    if (teamMemberArr[i] != null) {
                        continue;
                    }
                    teamMemberArr[i] = targetUser.getUserId();
                    break;
                }
                teamUser.getPlayTeam().setTeamNumber(teamUser.getPlayTeam().getTeamNumber() + 1);
                log.info("用户 {} 加入 {} 的队伍;", targetUser.getUserName(), teamUser.getUserName());

                taskPublicMethod.listener(targetUser);

                GameMsg.UserJoinTeamPerformResult userJoinTeamResult = newBuilder.setIsJoin(true).build();
                targetUser.getCtx().writeAndFlush(userJoinTeamResult);
            } else {
                log.info("用户 {} 加入 {} 队伍失败;", targetUser.getUserName(), teamUser.getUserName());
                enterTeamFail(targetUser.getCtx());
            }
        }
    }


    public void enterTeamFail(ChannelHandlerContext ctx) {
        GameMsg.UserEnterTeamFailResult userEnterTeamFailResult = GameMsg.UserEnterTeamFailResult.newBuilder().build();
        ctx.writeAndFlush(userEnterTeamFailResult);
    }

}
