package server.timer;

import constant.MonsterConst;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import server.Broadcast;
import server.UserManager;
import server.model.profession.SummonMonster;
import server.model.scene.Monster;
import server.model.User;
import server.model.scene.Scene;
import server.scene.GameData;
import type.ProfessionType;
import util.CustomizeThreadFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author 张丰博
 * 怪 攻击用户 定时器
 */
@Slf4j
public class MonsterTimer {
    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("怪攻击")
    );

    private static final MonsterTimer MONSTER_ATTACK_TIMER = new MonsterTimer();

    /**
     * 复活定时器
     */
    private Set<Integer> resurrectionMonsterSceneId = new HashSet<>();

    public static MonsterTimer getInstance() {
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
                                user.calCurrHp();
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
     * 复活野外怪
     * @param monsterCollection
     * @param sceneId
     */
    public void resurrectionMonster(Collection<Monster> monsterCollection, Integer sceneId) {
        if (monsterCollection == null || sceneId == null) {
            return;
        }

        Scene scene = GameData.getInstance().getSceneMap().get(sceneId);
        if (scene == null) {
            return;
        }

        //使用set记录正在复活的场景id， 若包含则直接返回
        synchronized (MonsterTimer.class) {
            if (resurrectionMonsterSceneId.contains(sceneId)) {

                log.info("场景 {} 中的怪正在复活ing;", scene.getName());
                return;
            }
            resurrectionMonsterSceneId.add(sceneId);
            log.info("{} 场景,添加复活定时器;",scene.getName());
        }


        scheduledThreadPool.schedule(() -> {

            for (Monster monster : monsterCollection) {
                synchronized (monster.getSubHpMonitor()) {
                    monster.setHp(MonsterConst.HP);
                }
            }

            log.info("场景 {} 中的怪正在复活;", scene.getName());
            GameMsg.MonsterResurrectionResult monsterResurrectionResult = GameMsg.MonsterResurrectionResult
                    .newBuilder()
                    .setSceneId(sceneId)
                    .build();
            Broadcast.broadcast(sceneId, monsterResurrectionResult);
        }, MonsterConst.RESURRECTION, TimeUnit.MINUTES);
    }


}
