package server.cmdhandler.skill.warrior;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.skill.ISkill;
import server.model.PlayArena;
import server.model.User;
import server.model.duplicate.BossMonster;
import server.model.duplicate.Duplicate;
import server.model.duplicate.ForceAttackUser;
import server.model.profession.Skill;
import server.model.profession.skill.WarriorSkillProperty;
import server.model.scene.Monster;
import server.scene.GameData;
import server.timer.BossAttackTimer;
import server.timer.MonsterTimer;
import util.MyUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author 张丰博
 *
 * 嘲讽
 *
 */
@Component
@Slf4j
public class RidiculeHandler implements ISkill {
    @Override
    public void skillHandle(ChannelHandlerContext ctx, GameMsg.UserSkillAttkCmd cmd) {

        MyUtil.checkIsNull(ctx, cmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        System.out.println("嘲讽技能;");

        //先判断是否有副本
        Duplicate currDuplicate = PublicMethod.getInstance().getDuplicate(user);
        //竞技场
        PlayArena playArena = user.getPlayArena();
        // 此时在野外
        Map<Integer, Monster> monsterMap = GameData.getInstance().getSceneMap().get(user.getCurSceneId()).getMonsterMap();

        Skill skill = user.getSkillMap().get(cmd.getSkillId());
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
