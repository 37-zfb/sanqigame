package server.timer;

import lombok.extern.slf4j.Slf4j;
import server.model.duplicate.BossMonster;
import server.model.duplicate.Duplicate;
import server.model.profession.SummonMonster;
import server.model.scene.Monster;
import server.model.scene.Scene;
import server.scene.GameData;
import server.PublicMethod;
import server.model.User;
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
     * 吟唱
     * @param user
     */
    public void startAttack(User user, SummonMonster summonMonster) {
        RunnableScheduledFuture<?> scheduledFuture =
                (RunnableScheduledFuture<?>) scheduledThreadPool
                        .scheduleAtFixedRate(() -> {
                            Duplicate duplicate = user.getCurrDuplicate();
                            Map<Integer, SummonMonster> summonMonsterMap = user.getSummonMonsterMap();

                            if (summonMonster.getHp() <= 0) {
                                summonMonsterMap.remove(summonMonster.getSkillId());
                                log.info("召唤兽已死,当前血量 {}", summonMonster.getHp());
                                // 取消定时器
                                user.getSummonMonsterRunnableScheduledFutureMap().get(summonMonster).cancel(true);
                            } else {
                                int subHp = summonMonster.calMonsterSubHp();
                                if (duplicate != null){
                                    //int subHp = 5000;
                                    // 副本
                                    BossMonster currBossMonster = duplicate.getCurrBossMonster();
                                    synchronized (currBossMonster.getATTACK_BOSS_MONITOR()) {
                                        PublicMethod.getInstance().normalOrSkillAttackBoss(user, duplicate, subHp, summonMonster);
                                    }
                                    log.info("召唤兽攻击 {} ,伤害 {} 剩余血量 {}", currBossMonster.getBossName(), subHp, currBossMonster.getHp());
                                }else if (user.getPlayArena() != null){
                                    // 竞技场

                                }else {
                                    // 公共地图
                                    Scene scene = GameData.getInstance().getSceneMap().get(user.getUserId());
                                    List<Monster> monsterAliveList = PublicMethod.getInstance().getMonsterAliveList(scene.getMonsterMap().values());
                                    Monster monster = monsterAliveList.remove((int) (Math.random() * monsterAliveList.size()));
                                    PublicMethod.getInstance().userOrSummonerAttackMonster(user,monster,summonMonster,summonMonster.calMonsterSubHp());
                                }

                            }
                        }, 3000, 3000, TimeUnit.MILLISECONDS);

        user.getSummonMonsterRunnableScheduledFutureMap().put(summonMonster, scheduledFuture);
    }

    public void cancelTimer(User user, SummonMonster summonMonster, Integer time) {
        if (summonMonster == null) {
            return;
        }
        scheduledThreadPool.schedule(() -> {
            user.getSummonMonsterRunnableScheduledFutureMap().get(summonMonster).cancel(true);
        }, time, TimeUnit.SECONDS);
    }


}