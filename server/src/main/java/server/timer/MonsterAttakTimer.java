package server.timer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.AnnotationUtils;
import server.model.UserManager;
import server.model.duplicate.ForceAttackUser;
import server.model.profession.SummonMonster;
import server.model.scene.Monster;
import server.model.User;
import type.ProfessionType;
import util.CustomizeThreadFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author 张丰博
 * 怪 攻击用户 定时器
 */
@Slf4j
public class MonsterAttakTimer {
    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("怪攻击")
    );

    private static final MonsterAttakTimer MONSTER_ATTACK_TIMER = new MonsterAttakTimer();

    public static MonsterAttakTimer getInstance() {
        return MONSTER_ATTACK_TIMER;
    }

    /**
     * @return
     */
    public void monsterNormalAttk(Monster monster) {
        if (monster == null) {
            return;
        }
        RunnableScheduledFuture<?> scheduledFuture =
                (RunnableScheduledFuture<?>) scheduledThreadPool
                        .scheduleAtFixedRate(() -> {

                            User user = null;
                            Integer forceId = monster.getForceId();
                            if (forceId != null) {
                                user = UserManager.getUserById(forceId);
                            }

                            SummonMonster summonMonster = null;
                            if (user == null) {
                                summonMonster = monster.chooseSummonMonster();
                                user = monster.chooseUser();
                            }

                            // 都为空，取消定时器
                            if (user == null && summonMonster == null) {
                                log.info("{} 没有要攻击的对象;", monster.getName());
                                monster.getRunnableScheduledFuture().cancel(true);
                                monster.setRunnableScheduledFuture(null);
                                return;
                            }

                            //都不为空,选择对monster伤害最高的
                            if (user != null && summonMonster != null) {
                                if (monster.getSummonMonsterMap().get(summonMonster) < monster.getUserIdMap().get(user.getUserId())) {
                                    summonMonster = null;
                                } else {
                                    user = null;
                                }
                            }

                            // 如果是牧师，并且在吟唱状态，此时受到攻击，定时器加血加蓝取消
                            if (user != null && user.getProfessionId() == ProfessionType.Pastor.getId()) {
                                if (user.getIsPrepare() != null) {
                                    user.getIsPrepare().cancel(true);
                                    user.setIsPrepare(null);
                                }
                            }

                            int subHp = monster.calUserSubHp();
                            // 怪的普通攻击
                            if (user != null) {
                                user.monsterAttackSubHp(monster.getName(), subHp);
                                log.info("用户 {} 受到 {} 攻击,减血 {},剩余血量 {}", user.getUserName(), monster.getName(), subHp, user.getCurrHp());
                            } else {
                                // 召唤兽
                                summonMonster.monsterAttackSubHp(subHp);
                                log.info("召唤兽 受到 {} 攻击,减血 {},剩余血量 {}", summonMonster.getName(), subHp, summonMonster.getHp());
                            }
                        }, 0, 2000, TimeUnit.MILLISECONDS);

        monster.setRunnableScheduledFuture(scheduledFuture);
    }


    /**
     * 移除指定 任务
     *
     * @param runnableScheduledFuture
     */
    public void removeTask(RunnableScheduledFuture runnableScheduledFuture) {
        if (runnableScheduledFuture == null) {
            return;
        }
        scheduledThreadPool.remove(runnableScheduledFuture);
    }


}
