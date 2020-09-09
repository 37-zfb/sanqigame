package server.cmdhandler.skill;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
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

        User user = PublicMethod.getInstance().getUser(ctx);

        //先判断是否有副本
        Duplicate currDuplicate = PublicMethod.getInstance().getDuplicate(user);

        Skill skill = user.getSkillMap().get(skillId);
        skill.setLastUseTime(System.currentTimeMillis());
        WarriorSkillProperty skillProperty = (WarriorSkillProperty) skill.getSkillProperty();
        // 此时在场景中;
        // 在公共地图
        Map<Integer, Monster> monsterMap = GameData.getInstance().getSceneMap().get(user.getCurSceneId()).getMonsterMap();
        // 存活着的怪
        List<Monster> monsterAliveList = PublicMethod.getInstance().getMonsterAliveList(monsterMap.values());
        for (WarriorSkillType skillType : WarriorSkillType.values()) {
            if (!skillType.getId().equals(skillProperty.getId())) {
                continue;
            }
            try {
                Method declaredMethod;
                if (currDuplicate == null) {
                    // 公共地图中
                    declaredMethod = WarriorSkillHandler.class.getDeclaredMethod(skillType.getName() + "SkillScene", List.class, User.class, Integer.class);
                    declaredMethod.invoke(this,monsterAliveList, user, skillId);

                } else {
                    //
                    declaredMethod = WarriorSkillHandler.class.getDeclaredMethod(skillType.getName() + "Skill", User.class, Duplicate.class, Integer.class);
                    declaredMethod.invoke(this, user, currDuplicate, skillId);

                }
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

    /**
     * 嘲讽当前场景中的所有存活在的怪
     *
     * @param monsterAliveList 存活怪的集合
     * @param user             用户对象
     * @param skillId          技能id
     */
    private void ridiculeSkillScene(List<Monster> monsterAliveList, User user, Integer skillId) {
        Skill skill = user.getSkillMap().get(skillId);
        WarriorSkillProperty skillProperty = (WarriorSkillProperty) skill.getSkillProperty();
        if (monsterAliveList.size() != 0) {
            // 嘲讽当前场景中所有的怪
            for (Monster monster : monsterAliveList) {
                // 后端架构如果采用多线程，怪减血时要加synchronized
                synchronized (monster.getSubHpMonitor()) {
                    if (monster.isDie()) {
                        // 有可能刚被前一用户杀死，
                        // 怪死，减蓝、技能设为cd; 重新定义恢复终止时间
                        user.subMp(skill.getConsumeMp());
                        log.info("{} 已被其他玩家击杀!", monster.getName());
                    } else {
                        ForceAttackUser forceAttackUser = PublicMethod.getInstance().createForeAttackUser(user.getUserId(), skillProperty.getEffectTime());
                        AtomicReference<ForceAttackUser> attackUserAtomicReference = monster.getAttackUserAtomicReference();
                        boolean isSuccess = attackUserAtomicReference.compareAndSet(null, forceAttackUser);

                        // 减蓝
                        user.subMp(skill.getConsumeMp());
                        log.info("用户 {} 在 {} 释放 {} 吸引 {} 火力；", user.getUserName(), GameData.getInstance().getSceneMap().get(user.getCurSceneId()).getName(), skill.getName(), monster.getName());
                        GameMsg.UserSkillAttkResult.Builder newBuilder = GameMsg.UserSkillAttkResult.newBuilder();
                        GameMsg.UserSkillAttkResult userSkillAttkResult = newBuilder.setIsSuccess(isSuccess)
                                .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                                .setSubtractHp(0)
                                .build();
                        user.getCtx().writeAndFlush(userSkillAttkResult);
                    }
                }
                // 减蓝
                user.subMp(skill.getConsumeMp());
            }
        }
    }

    /**
     * 嘲讽当前副本中的boss
     *
     * @param user      用户对象
     * @param duplicate 副本集合
     * @param skillId   技能id
     */
    private void ridiculeSkill(User user, Duplicate duplicate, Integer skillId) {
        Skill skill = user.getSkillMap().get(skillId);
        WarriorSkillProperty skillProperty = (WarriorSkillProperty) skill.getSkillProperty();

        // 吸引boss火力
        // 副本中;  设置自己成为boss强制攻击的玩家
        BossMonster currBossMonster = duplicate.getCurrBossMonster();
        ForceAttackUser forceAttackUser = PublicMethod.getInstance().createForeAttackUser(user.getUserId(), skillProperty.getEffectTime());
        AtomicReference<ForceAttackUser> attackUserAtomicReference = currBossMonster.getAttackUserAtomicReference();
        boolean isSuccess = attackUserAtomicReference.compareAndSet(null, forceAttackUser);

        // 减蓝
        user.subMp(skill.getConsumeMp());

        log.info("用户 {} 释放 {} 吸引boss火力；", user.getUserName(), skill.getName());
        GameMsg.UserSkillAttkResult.Builder newBuilder = GameMsg.UserSkillAttkResult.newBuilder();
        GameMsg.UserSkillAttkResult userSkillAttkResult = newBuilder.setIsSuccess(isSuccess)
                .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                .setSubtractHp(0)
                .build();
        user.getCtx().writeAndFlush(userSkillAttkResult);
    }

    /**
     * 竞技场嘲讽
     */
    private void ridiculeSkillArena() {

    }
}
