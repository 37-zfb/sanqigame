package server.cmdhandler.skill;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import server.model.PlayArena;
import server.model.duplicate.BossMonster;
import server.model.duplicate.Duplicate;
import server.model.duplicate.ForceAttackUser;
import server.model.profession.Skill;
import server.model.profession.skill.WarriorSkillProperty;
import server.model.scene.Monster;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.scene.GameData;
import server.PublicMethod;
import server.model.User;
import server.timer.BossAttackTimer;
import server.timer.MonsterTimer;
import type.skill.WarriorSkillType;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author 张丰博
 * 战士技能处理类
 * 能进入此类中的都能够释放技能
 */
@Component
@Slf4j
public class WarriorSkillHandler implements ISkillHandler<WarriorSkillProperty> {

    @Override
    public void skillHandle(ChannelHandlerContext ctx, WarriorSkillProperty warriorSkillProperty, Integer skillId) {

        if (ctx == null || warriorSkillProperty == null || skillId == null) {
            return;
        }

        User user = PublicMethod.getInstance().getUser(ctx);
        Skill skill = user.getSkillMap().get(skillId);
        if (skill == null) {
            return;
        }

        skill.setLastUseTime(System.currentTimeMillis());
        WarriorSkillProperty skillProperty = (WarriorSkillProperty) skill.getSkillProperty();

        for (WarriorSkillType skillType : WarriorSkillType.values()) {
            if (!skillType.getId().equals(skillProperty.getId())) {
                continue;
            }
            try {
                Method declaredMethod = WarriorSkillHandler.class.getDeclaredMethod(skillType.getName() + "Skill", User.class, Integer.class);
                declaredMethod.invoke(this, user, skillId);
                break;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

    }

    /**
     * 嘲讽当前副本中的boss
     *
     * @param user    用户对象
     * @param skillId 技能id
     */
    private void ridiculeSkill(User user, Integer skillId) {
        if (user == null || skillId == null) {
            return;
        }

        //先判断是否有副本
        Duplicate currDuplicate = PublicMethod.getInstance().getDuplicate(user);
        //竞技场
        PlayArena playArena = user.getPlayArena();
        // 此时在野外
        Map<Integer, Monster> monsterMap = GameData.getInstance().getSceneMap().get(user.getCurSceneId()).getMonsterMap();

        Skill skill = user.getSkillMap().get(skillId);
        WarriorSkillProperty skillProperty = (WarriorSkillProperty) skill.getSkillProperty();

        if (currDuplicate != null) {
            // 副本中;  设置自己成为boss强制攻击的玩家
            BossMonster currBossMonster = currDuplicate.getCurrBossMonster();
            ForceAttackUser forceAttackUser = PublicMethod.getInstance().createForeAttackUser(user.getUserId(), skillProperty.getEffectTime());
            AtomicReference<ForceAttackUser> attackUserAtomicReference = currBossMonster.getAttackUserAtomicReference();
            boolean isSuccess = attackUserAtomicReference.compareAndSet(null, forceAttackUser);

            if (isSuccess) {
                log.info("用户 {} 在副本 {} 释放 {} 吸引 {} 火力；",
                        user.getUserName(),
                        currDuplicate.getName(),
                        skill.getName(),
                        currBossMonster.getBossName());
            }

            if (currBossMonster.getScheduledFuture() == null) {
                //设置boss定时器， 攻击玩家
                BossAttackTimer.getInstance().bossNormalAttack(currBossMonster,user);
            }

        } else if (playArena != null) {
        } else if (monsterMap.size() != 0) {
            //野外
            List<Monster> monsterAliveList = PublicMethod.getInstance().getMonsterAliveList(monsterMap.values());
            if (monsterAliveList == null) {
                return;
            }

            // 嘲讽当前场景中所有的怪
            for (Monster monster : monsterAliveList) {
                synchronized (monster.getSubHpMonitor()) {
                    if (monster.isDie()) {
                        log.info("{} 已被其他玩家击杀!", monster.getName());
                        continue;
                    }
                }

                ForceAttackUser forceAttackUser = PublicMethod.getInstance()
                        .createForeAttackUser(user.getUserId(), skillProperty.getEffectTime());
                AtomicReference<ForceAttackUser> attackUserAtomicReference = monster.getAttackUserAtomicReference();
                boolean isSuccess = attackUserAtomicReference.compareAndSet(null, forceAttackUser);

                if (isSuccess) {
                    log.info("用户 {} 在 {} 释放 {} 吸引 {} 火力；",
                            user.getUserName(),
                            GameData.getInstance().getSceneMap().get(user.getCurSceneId()).getName(),
                            skill.getName(),
                            monster.getName());

                }

                if (monster.getRunnableScheduledFuture() == null) {
                    // 定时器为null,设置boss定时器， 攻击玩家
                    MonsterTimer.getInstance().monsterNormalAttk(monster);
                }
            }
        }

        // 减蓝
        user.subMp(skill.getConsumeMp());

        GameMsg.UserSkillAttkResult.Builder newBuilder = GameMsg.UserSkillAttkResult.newBuilder();
        GameMsg.UserSkillAttkResult userSkillAttkResult = newBuilder.setIsSuccess(true)
                .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                .setSubMp(skill.getConsumeMp())
                .setSubtractHp(0)
                .build();
        user.getCtx().writeAndFlush(userSkillAttkResult);
    }


}
