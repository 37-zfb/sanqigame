package server.cmdhandler.skill.sorceress;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.UserManager;
import server.cmdhandler.arena.ArenaUtil;
import server.cmdhandler.skill.ISkill;
import server.cmdhandler.task.listener.TaskUtil;
import server.model.PlayArena;
import server.model.User;
import server.model.duplicate.Duplicate;
import server.model.profession.Skill;
import server.model.profession.skill.SorceressSkillProperty;
import server.model.scene.Monster;
import server.scene.GameData;
import server.timer.MonsterTimer;
import type.TaskType;
import util.MyUtil;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 * 全体攻击
 */
@Component
@Slf4j
public class GroupAttackHandler implements ISkill {

    @Autowired
    private TaskUtil taskUtil;

    @Override
    public void skillHandle(ChannelHandlerContext ctx, GameMsg.UserSkillAttkCmd cmd) {
        MyUtil.checkIsNull(ctx, cmd);
        User user = PublicMethod.getInstance().getUser(ctx);


        //先判断是否有副本
        Duplicate currDuplicate = PublicMethod.getInstance().getDuplicate(user);
        //竞技场
        PlayArena playArena = user.getPlayArena();
        // 在公共地图
        Map<Integer, Monster> monsterMap = GameData.getInstance().getSceneMap().get(user.getCurSceneId()).getMonsterMap();

        Skill skill = user.getSkillMap().get(cmd.getSkillId());
        SorceressSkillProperty skillProperty = (SorceressSkillProperty) skill.getSkillProperty();

        int equDamage = user.getEquDamage();
        int subHp = (int) (Math.random() * (user.getBaseDamage() * skillProperty.getDamagePercent() + skillProperty.getDamageValue() + user.getBaseDamage()) + equDamage * 10) + 500;


        if (currDuplicate != null) {
            PublicMethod.getInstance().normalOrSkillAttackBoss(user, currDuplicate, subHp, null);
        } else if (playArena != null) {

            Integer targetUserId = playArena.getTargetUserId();
            User targetUser = UserManager.getUserById(targetUserId);
            if (targetUser == null) {
                user.getPlayArena().setTargetUserId(null);
                GameMsg.UserDieResult userDieResult = GameMsg.UserDieResult.newBuilder()
                        .setTargetUserId(targetUser.getUserId())
                        .build();
                user.getCtx().writeAndFlush(userDieResult);
                return;
            }

            synchronized (targetUser.getHpMonitor()) {
                synchronized (targetUser.getSHIELD_MONITOR()){
                    if (targetUser.getShieldValue() > subHp) {
                        targetUser.setShieldValue(targetUser.getShieldValue() - subHp);

                        PublicMethod.getInstance().sendShieldMsg(subHp, targetUser);
                    } else if (targetUser.getShieldValue() > 0 && targetUser.getShieldValue() < subHp) {
                        subHp -= targetUser.getShieldValue();
                        PublicMethod.getInstance().sendShieldMsg(targetUser.getShieldValue(), targetUser);

                        targetUser.setShieldValue(0);

                        targetUser.setCurrHp(targetUser.getCurrHp() - subHp);
                    } else {
                        targetUser.setCurrHp(targetUser.getCurrHp() - subHp);
                    }
                }
            }


            ArenaUtil.getArenaUtil().sendMsg(user, targetUser, subHp);

            taskUtil.listener(user, TaskType.PKWin.getTaskCode());

        } else if (monsterMap.size() != 0) {
            // 存活着的怪
            List<Monster> monsterAliveList = PublicMethod.getInstance().getMonsterAliveList(monsterMap.values());
            if (monsterAliveList == null) {
                return;
            }

            for (Monster monster : monsterAliveList) {
                PublicMethod.getInstance().userOrSummonerAttackMonster(user, monster, null, subHp);
            }

            monsterAliveList = PublicMethod.getInstance().getMonsterAliveList(monsterMap.values());
            if (monsterAliveList.size() == 0) {
                MonsterTimer.getInstance().resurrectionMonster(monsterMap.values(), user.getCurSceneId());
            }

        }

        // 减蓝
        user.subMp(skill.getConsumeMp());

        log.info("用户 {} 释放 {} 攻击全部;", user.getUserName(), skill.getName());
        GameMsg.UserSkillAttkResult.Builder newBuilder = GameMsg.UserSkillAttkResult.newBuilder();
        GameMsg.UserSkillAttkResult userSkillAttkResult = newBuilder.setIsSuccess(true)
                .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                .setSubMp(skill.getConsumeMp())
                .setSubtractHp(0)
                .build();
        user.getCtx().writeAndFlush(userSkillAttkResult);

    }
}
