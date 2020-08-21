package server.cmdhandler.duplicate;

import constant.DuplicateConst;
import constant.ProfessionConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import model.duplicate.BossMonster;
import model.duplicate.Duplicate;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayTeam;
import server.model.User;
import server.model.UserManager;
import server.timer.BossAttackTimer;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public class UserQuitTeamAndDuplicateCmdHandler implements ICmdHandler<GameMsg.UserQuitTeamAndDuplicateCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserQuitTeamAndDuplicateCmd userQuitTeamAndDuplicateCmd) {
        MyUtil.checkIsNull(ctx, userQuitTeamAndDuplicateCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        // 退出队伍  ，退出副本， 不取消定时器，

        PlayTeam playTeam = user.getPlayTeam();
        Integer[] team_member = playTeam.getTEAM_MEMBER();
        for (int i = 0; i < team_member.length; i++) {
            if (team_member[i] != null && team_member[i].equals(user.getUserId())) {
                team_member[i] = null;
                break;
            }
        }
        Duplicate currDuplicate = playTeam.getCurrDuplicate();
        // 若当前副本不为空
        BossMonster currBossMonster = currDuplicate.getCurrBossMonster();
        synchronized (currBossMonster.getCHOOSE_USER_MONITOR()) {
            Map<Integer, Integer> userIdMap = currBossMonster.getUserIdMap();
            userIdMap.remove(user.getUserId());
        }

        if (playTeam.getTeamLeaderId().equals(user.getUserId())){
            team_member = playTeam.getTEAM_MEMBER();
            for (int i = 0; i < team_member.length; i++) {
                if (team_member[i] != null){
                    playTeam.setTeamLeaderId(team_member[i]);
                }
            }
        }



        //取消召唤师定时器
        PublicMethod.getInstance().cancelSummonTimer(user);

        user.setPlayTeam(null);
        user.setCurrHp(ProfessionConst.HP);
        user.setCurrMp(ProfessionConst.MP);

        //通知其他队伍成员
        GameMsg.UserInfo.Builder userInfo = GameMsg.UserInfo.newBuilder()
                .setUserId(user.getUserId())
                .setUserName(user.getUserName());
        GameMsg.UserQuitTeamResult userQuitTeamResult = GameMsg.UserQuitTeamResult.newBuilder()
                .setUserInfo(userInfo)
                .setTeamLeaderId(playTeam.getTeamLeaderId())
                .build();
        for (Integer id : team_member) {
            if (id != null) {
                User userById = UserManager.getUserById(id);
                userById.getCtx().writeAndFlush(userQuitTeamResult);
            }
        }

        GameMsg.UserQuitTeamAndDuplicateResult build = GameMsg.UserQuitTeamAndDuplicateResult.newBuilder().build();
        ctx.writeAndFlush(build);

    }
}
