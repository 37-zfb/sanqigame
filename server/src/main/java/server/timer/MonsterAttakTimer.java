package server.timer;

import lombok.extern.slf4j.Slf4j;
import server.model.profession.SummonMonster;
import server.model.scene.Monster;
import server.model.User;
import type.ProfessionType;
import util.CustomizeThreadFactory;

import java.util.concurrent.*;

/**
 * @author 张丰博
 *  怪 攻击用户 定时器
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

        RunnableScheduledFuture<?> scheduledFuture =
                (RunnableScheduledFuture<?>) scheduledThreadPool
                        .scheduleAtFixedRate(() -> {

                            SummonMonster summonMonster = monster.chooseSummonMonster();
                            User user = monster.chooseUser();

                            // 挑选伤害最高者
                            Integer summonMonsterSub = monster.getSummonMonsterMap().get(summonMonster);
                            if ((summonMonster ==null ? 0 : summonMonsterSub) < monster.getUserIdMap().get(user.getUserId())) {
                                summonMonster = null;
                            } else {
                                user = null;
                            }

                            // 都为空，取消定时器
                            if (user == null && summonMonster == null){
                                monster.getRunnableScheduledFuture().cancel(true);
                                return;
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
                                user.monsterAttackSubHp(monster.getName(),subHp);
                                log.info("用户 {} 受到 {} 攻击,减血 {},剩余血量 {}",user.getUserName(),monster.getName(),subHp,user.getCurrHp());
                            } else {
                                // 召唤兽
                                summonMonster.monsterAttackSubHp(subHp);
                                log.info("召唤兽 受到 {} 攻击,减血 {},剩余血量 {}",summonMonster.getName(),subHp,summonMonster.getHp());
                            }

                        }, 3000, 3000, TimeUnit.MILLISECONDS);

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
