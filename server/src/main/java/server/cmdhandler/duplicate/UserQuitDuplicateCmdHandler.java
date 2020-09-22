package server.cmdhandler.duplicate;

import constant.DuplicateConst;
import constant.ProfessionConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayTeam;
import server.model.User;
import server.model.duplicate.Duplicate;
import server.model.profession.Skill;
import server.timer.BossAttackTimer;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 * 退出副本
 */
@Component
@Slf4j
public class UserQuitDuplicateCmdHandler implements ICmdHandler<GameMsg.UserQuitDuplicateCmd> {

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserQuitDuplicateCmd userQuitDuplicateCmd) {

        MyUtil.checkIsNull(ctx, userQuitDuplicateCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        Duplicate currDuplicate;
        PlayTeam playTeam = user.getPlayTeam();
        if (playTeam == null) {
            currDuplicate = user.getCurrDuplicate();
            user.setCurrDuplicate(null);
        } else {
            currDuplicate = playTeam.getCurrDuplicate();
            playTeam.setCurrDuplicate(null);
        }
        if (currDuplicate != null) {
            // 取消定时器
            BossAttackTimer.getInstance().cancelTask(currDuplicate.getCurrBossMonster().getScheduledFuture());
        }

        //取消掉血定时器
        if (user.getSubHpTask() != null) {
            user.getSubHpTask().cancel(true);
            user.setSubHpNumber(0);
        }

//        // 取消召唤师定时器
        PublicMethod.getInstance().cancelSummonTimer(user);

        user.setCurrHp(ProfessionConst.HP);
        user.setCurrMp(ProfessionConst.MP);
        user.setShieldValue(0);

        Map<Integer, Skill> skillMap = user.getSkillMap();
        skillMap.values().forEach(skill->skill.setLastUseTime(0L));

        //持久化装备耐久度
        PublicMethod.getInstance().dbWeaponDurability(user.getUserEquipmentArr());

        GameMsg.UserQuitDuplicateResult.Builder newBuilder = GameMsg.UserQuitDuplicateResult.newBuilder();
        if (user.getCurrHp() <= 0) {
            newBuilder.setQuitDuplicateType(DuplicateConst.USER_KILLED);
        } else {
            newBuilder.setQuitDuplicateType(DuplicateConst.USER_NORMAL_QUIT_DUPLICATE);
        }

        // 用户退出
        GameMsg.UserQuitDuplicateResult userQuitDuplicateResult = newBuilder.build();

        ctx.writeAndFlush(userQuitDuplicateResult);

    }
}
