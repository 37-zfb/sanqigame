package server.timer;

import lombok.extern.slf4j.Slf4j;
import server.model.duplicate.BossMonster;
import server.model.profession.SummonMonster;
import server.model.User;
import type.ProfessionType;
import util.CustomizeThreadFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 * boss攻击定时器
 */
@Slf4j
public class BossAttackTimer {
    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("怪攻击")
    );

    private static final BossAttackTimer BOSS_ATTACK_TIMER = new BossAttackTimer();

    public static BossAttackTimer getInstance() {
        return BOSS_ATTACK_TIMER;
    }


    /**
     * boss 攻击
     *
     * @param bossMonster
     */
    public void bossNormalAttack(BossMonster bossMonster) {
        if (bossMonster == null) {
            return;
        }
        ScheduledFuture<?> scheduledFuture =
                scheduledThreadPool
                        .scheduleAtFixedRate(() -> {
                            // 选择当前要攻击的user
                            User user = bossMonster.chooseUser();
                            SummonMonster summonMonster = bossMonster.chooseSummonMonster();

                            // 挑选伤害最高者
                            if (bossMonster.getSummonMonsterMap().get(summonMonster) < bossMonster.getUserIdMap().get(user.getUserId())) {
                                summonMonster = null;
                            } else {
                                user = null;
                            }


                            if (user == null && summonMonster == null) {
                                // 此时用户全部阵亡
                                log.info("用户全部阵亡;");
                                bossMonster.getScheduledFuture().cancel(true);
                                return;
                            }

                            // 如果是牧师，并且在吟唱状态，此时受到攻击，定时器加血加蓝取消
                            if (user != null && user.getProfessionId() == ProfessionType.Pastor.getId()) {
                                if (user.getIsPrepare() != null) {
                                    user.getIsPrepare().cancel(true);
                                    user.setIsPrepare(null);
                                }
                            }

                            int subHp = 0;
                            if (user != null) {
                                subHp = bossMonster.calUserSubHp(user.getBaseDefense(), user.getWeakenDefense());
//                            int subHp = 5000;
                                user.bossAttackSubHp(bossMonster,subHp);

                            } else {
                                // 召唤兽
                                subHp = bossMonster.calUserSubHp(summonMonster.getBaseDefense(), summonMonster.getWeakenDefense());
//                                subHp = 5000;
                                summonMonster.bossAttackSubHp(bossMonster,subHp);

                            }
                        }, 0, 2000, TimeUnit.MILLISECONDS);

        bossMonster.setScheduledFuture(scheduledFuture);
    }


    /**
     * 取消 任务
     *
     * @param scheduledFuture
     */
    public void cancelTask(ScheduledFuture scheduledFuture) {
        if (scheduledFuture == null) {
            return;
        }
        scheduledFuture.cancel(true);
    }
}
