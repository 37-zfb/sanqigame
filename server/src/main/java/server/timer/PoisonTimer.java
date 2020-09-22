package server.timer;

import constant.BossMonsterConst;
import constant.DuplicateConst;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import server.GameServer;
import server.PublicMethod;
import server.UserManager;
import server.cmdhandler.arena.ArenaUtil;
import server.cmdhandler.task.listener.TaskUtil;
import server.model.PlayArena;
import server.model.User;
import server.model.duplicate.BossMonster;
import server.model.duplicate.Duplicate;
import server.model.profession.Skill;
import server.model.scene.Monster;
import server.scene.GameData;
import util.CustomizeThreadFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 * 中毒
 */
@Slf4j
public final class PoisonTimer {

    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("中毒线程")
    );
    private final TaskUtil taskUtil = GameServer.APPLICATION_CONTEXT.getBean(TaskUtil.class);

    private static final PoisonTimer POISON_TIMER = new PoisonTimer();

    public static PoisonTimer getInstance() {
        return POISON_TIMER;
    }

    private PoisonTimer() {
    }

    /**
     * 技能执行
     *
     * @param user
     * @param skillId
     */
    public void poisonSkill(User user, Integer skillId) {
        //先判断是否有副本
        Duplicate currDuplicate = PublicMethod.getInstance().getDuplicate(user);
        //竞技场
        PlayArena playArena = user.getPlayArena();
        // 在公共地图
        Map<Integer, Monster> monsterMap = GameData.getInstance().getSceneMap().get(user.getCurSceneId()).getMonsterMap();

        Skill skill = user.getSkillMap().get(skillId);

        if (currDuplicate != null) {
            BossMonster currBossMonster = currDuplicate.getCurrBossMonster();
            bossPoison(user, currBossMonster, currDuplicate, 0);
            bossPoison(user, currBossMonster, currDuplicate, 1);
            bossPoison(user, currBossMonster, currDuplicate, 2);
            bossPoison(user, currBossMonster, currDuplicate, 3);
            bossPoison(user, currBossMonster, currDuplicate, 4);
        } else if (playArena != null) {
            userPoison(user, 0);
            userPoison(user, 1);
            userPoison(user, 2);
            userPoison(user, 3);
            userPoison(user, 4);

            taskUtil.listener(user);
        } else if (monsterMap.size() != 0) {
            List<Monster> monsterAliveList = PublicMethod.getInstance().getMonsterAliveList(monsterMap.values());
            if (monsterAliveList == null) {
                return;
            }

            monsterAliveList = PublicMethod.getInstance().getMonsterAliveList(monsterMap.values());
            if (monsterAliveList.size() == 0) {
                // 野外怪  复活定时器
                MonsterTimer.getInstance().resurrectionMonster(monsterMap.values(), user.getCurSceneId());
                return;
            }

            Monster monster = monsterAliveList.remove((int) (Math.random() * monsterAliveList.size()));

            monsterPoison(user, monster, 0);
            monsterPoison(user, monster, 1);
            monsterPoison(user, monster, 2);
            monsterPoison(user, monster, 3);
            monsterPoison(user, monster, 4);
        }
    }

    private void monsterPoison(User user, Monster monster, Integer delayTime) {
        if (user == null || delayTime == null || monster == null) {
            return;
        }

        scheduledThreadPool.schedule(() -> {
            // 存活着的怪
            // 防止多线程执行时，减血超减
            int subHp = (int) (Math.random() * 100 + 300);

            if (subHp <= monster.getHp()) {
                PublicMethod.getInstance().userOrSummonerAttackMonster(user, monster, null, subHp);

            }


        }, delayTime, TimeUnit.SECONDS);
    }

    private void userPoison(User user, Integer delayTime) {

        if (user == null || delayTime == null) {
            return;
        }

        scheduledThreadPool.schedule(() -> {

            // 目标用户应减少的血量
            PlayArena playArena = user.getPlayArena();
            User targetUser = UserManager.getUserById(playArena.getTargetUserId());
            if (targetUser == null) {
                user.getPlayArena().setTargetUserId(null);
                GameMsg.UserDieResult userDieResult = GameMsg.UserDieResult.newBuilder()
                        .setTargetUserId(playArena.getTargetUserId())
                        .build();
                user.getCtx().writeAndFlush(userDieResult);
                return;
            }

            //boss中毒
            // 防止多线程执行时，减血超减
            synchronized (targetUser.getHpMonitor()) {
                //
                int subHp = (int) (Math.random() * 100 + 300);
                if (targetUser.getCurrHp() == 0) {
                    return;
                }

                targetUser.setCurrHp(targetUser.getCurrHp() - subHp);
                ArenaUtil.getArenaUtil().sendMsg(user, targetUser, subHp);

                log.info("用户 {} 中毒 减血 {},剩余血量 {};", user.getUserName(), subHp, user.getCurrHp());

            }

        }, delayTime, TimeUnit.SECONDS);
    }

    private void bossPoison(User user, BossMonster bossMonster, Duplicate duplicate, Integer delayTime) {
        if (user == null || bossMonster == null || delayTime == null) {
            return;
        }

        scheduledThreadPool.schedule(() -> {
            synchronized (bossMonster.getATTACK_BOSS_MONITOR()) {
                //
                int subHp = (int) (Math.random() * 100 + 300);
                if (bossMonster.getHp() == 0) {
                    return;
                }
                log.info("boss {} 中毒;", bossMonster.getBossName());
                PublicMethod.getInstance().normalOrSkillAttackBoss(user, duplicate, subHp, null);
            }
        }, delayTime, TimeUnit.SECONDS);
    }
}
