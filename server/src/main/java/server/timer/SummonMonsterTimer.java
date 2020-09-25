package server.timer;

import constant.SceneConst;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import server.GameServer;
import server.UserManager;
import server.cmdhandler.skill.SkillUtil;
import server.cmdhandler.task.listener.TaskUtil;
import server.model.duplicate.BossMonster;
import server.model.duplicate.Duplicate;
import server.model.profession.SummonMonster;
import server.model.scene.Monster;
import server.model.scene.Scene;
import server.scene.GameData;
import server.PublicMethod;
import server.model.User;
import type.SceneType;
import util.CustomizeThreadFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 */
@Slf4j
public class SummonMonsterTimer {

    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("召唤兽攻击线程")
    );

    private static final SummonMonsterTimer SUMMON_MONSTER_TIMER = new SummonMonsterTimer();

    public static SummonMonsterTimer getInstance() {
        return SUMMON_MONSTER_TIMER;
    }


    /**
     * 召唤兽攻击
     *
     * @param user
     */
    public void startAttack(User user, SummonMonster summonMonster) {
        if (user == null || summonMonster == null) {
            return;
        }

        RunnableScheduledFuture<?> scheduledFuture =
                (RunnableScheduledFuture<?>) scheduledThreadPool
                        .scheduleAtFixedRate(() -> {

                            Duplicate duplicate = PublicMethod.getInstance().getDuplicate(user);
                            Map<Integer, SummonMonster> summonMonsterMap = user.getSummonMonsterMap();

                            if (summonMonster.getHp() <= 0) {
                                summonMonsterMap.remove(summonMonster.getSkillId());
                                log.info("用户 {} 的召唤兽已死;", user.getUserName());
                                // 取消定时器
                                user.getSummonMonsterRunnableScheduledFutureMap().get(summonMonster).cancel(true);
                                user.getSummonMonsterRunnableScheduledFutureMap().remove(summonMonster);
                                return;
                            }

                            int subHp = summonMonster.calMonsterSubHp();
                            if (duplicate != null) {
                                // 副本
                                BossMonster currBossMonster = duplicate.getCurrBossMonster();
                                if (currBossMonster == null) {
                                    return;
                                }
                                //判断是否超时
                                SkillUtil.getSkillUtil().isTimeout(user, currBossMonster);


                                synchronized (currBossMonster.getATTACK_BOSS_MONITOR()) {
                                    if (currBossMonster.getHp() > 0) {
                                        PublicMethod.getInstance().normalOrSkillAttackBoss(user, duplicate, subHp, summonMonster);
                                    } else {
                                        log.info("当前boss {} 已死;", currBossMonster.getBossName());
                                    }
                                }
                                log.info("召唤兽攻击 {} ,伤害 {} 剩余血量 {}", currBossMonster.getBossName(), subHp, currBossMonster.getHp());
                            } else if (user.getPlayArena() != null) {
                                // 竞技场
                                Integer targetUserId = user.getPlayArena().getTargetUserId();
                                User targetUser = UserManager.getUserById(targetUserId);
                                if (targetUser == null) {
                                    return;
                                }

                                synchronized (targetUser.getHpMonitor()) {
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
                                log.info("用户:{}, 对用户:{} 的伤害 {}", user.getUserName(), targetUser.getUserName(), subHp);

                                GameMsg.UserSubtractHpResult userSubtractHpResult = GameMsg.UserSubtractHpResult.newBuilder()
                                        .setTargetUserId(targetUser.getUserId())
                                        .setSubtractHp(subHp)
                                        .build();
                                user.getCtx().writeAndFlush(userSubtractHpResult);
                                targetUser.getCtx().writeAndFlush(userSubtractHpResult);

                                if (targetUser.getCurrHp() <= 0) {
                                    targetUser.getPlayArena().setTargetUserId(null);
                                    user.getPlayArena().setTargetUserId(null);

                                    GameMsg.UserDieResult userDieResult = GameMsg.UserDieResult.newBuilder()
                                            .setTargetUserId(targetUser.getUserId())
                                            .build();
                                    user.getCtx().writeAndFlush(userDieResult);
                                    targetUser.getCtx().writeAndFlush(userDieResult);
                                }
                            } else if (SceneType.getSceneIdByType(SceneConst.FIELD).contains(user.getCurSceneId())) {
                                // 野外
                                Scene scene = GameData.getInstance().getSceneMap().get(user.getCurSceneId());
                                if (scene == null) {
                                    return;
                                }

                                List<Monster> monsterAliveList = PublicMethod.getInstance().getMonsterAliveList(scene.getMonsterMap().values());
                                if (monsterAliveList == null) {
                                    return;
                                }

                                Monster monster = monsterAliveList.remove((int) (Math.random() * monsterAliveList.size()));
                                if (monster == null) {
                                    return;
                                }

                                PublicMethod.getInstance().userOrSummonerAttackMonster(user, monster, summonMonster, summonMonster.calMonsterSubHp());

                                monsterAliveList = PublicMethod.getInstance().getMonsterAliveList(scene.getMonsterMap().values());
                                if (monsterAliveList.size() == 0) {
                                    MonsterTimer.getInstance().resurrectionMonster(scene.getMonsterMap().values(), user.getCurSceneId());
                                }
                            }

                        }, 3000, 3000, TimeUnit.MILLISECONDS);

        user.getSummonMonsterRunnableScheduledFutureMap().put(summonMonster, scheduledFuture);

    }

    public void cancelTimer(User user, SummonMonster summonMonster, Integer time) {
        if (summonMonster == null || user == null || time == null) {
            return;
        }
        scheduledThreadPool.schedule(() -> {
            synchronized (summonMonster.getSubHpMonitor()) {
                summonMonster.setHp(0);
            }
            user.getSummonMonsterRunnableScheduledFutureMap().get(summonMonster).cancel(true);
            user.getSummonMonsterRunnableScheduledFutureMap().remove(summonMonster);
        }, time, TimeUnit.SECONDS);
    }


}