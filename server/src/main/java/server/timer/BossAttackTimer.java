package server.timer;

import constant.BossMonsterConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import model.duplicate.BossMonster;
import msg.GameMsg;
import server.GameServer;
import server.cmdhandler.duplicate.BossSkillAttack;
import server.model.User;
import server.model.UserManager;
import server.service.UserService;
import util.CustomizeThreadFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.RunnableScheduledFuture;
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

    private final UserService userService = GameServer.APPLICATION_CONTEXT.getBean(UserService.class);

    public void bossNormalAttack(BossMonster bossMonster) {
        if (bossMonster == null) {
            return;
        }

        ScheduledFuture<?> scheduledFuture =
                scheduledThreadPool
                        .scheduleAtFixedRate(() -> {


                            // 选择当前要攻击的user
                            User user = null;
                            synchronized (bossMonster.getChooseUserMonitor()) {
                                Map<Integer, Integer> userIdMap = bossMonster.getUserIdMap();
                                Optional<Map.Entry<Integer, Integer>> max = userIdMap.entrySet().stream().max((e1, e2) -> {
                                    return -(e1.getValue().compareTo(e2.getValue()));
                                });
                                user = UserManager.getUserById(max.get().getKey());
                            }

                            // boss的普通攻击
                            int subHp = bossMonster.calUserSubHp(user.getBaseDefense(),user.getWeakenDefense());
//                            int subHp = 5000;

                            // 防止多线程执行时，减血超减
                            synchronized (user.getHpMonitor()) {

                                if (bossMonster.getOrdinaryAttack() > BossMonsterConst.ORDINARY_ATTACK) {
                                    //十秒后，防御属性回归正常
                                    user.setWeakenDefense(0);
                                    bossMonster.setOrdinaryAttack(0);
                                    // 每五次普通攻击，一次技能攻击
                                    BossSkillAttack.getInstance().bossSkillAttack(user, bossMonster);
                                }

                                // 用户减血
                                if (user.getCurrHp() <= 0 || (user.getCurrHp() - subHp) <= 0) {
                                    log.info("用户: {} 已死亡;", user.getUserName());
                                    user.setCurrHp(0);
                                    // 发送死亡消息
                                    GameMsg.DieResult dieResult = GameMsg.DieResult.newBuilder()
                                            .setTargetUserId(user.getUserId())
                                            .build();
                                    user.getCtx().writeAndFlush(dieResult);
                                } else {
                                    log.info("用户: {} , 当前血量: {} , 受到伤害减血: {}", user.getUserName(), user.getCurrHp(), subHp);
                                    // 普通攻击数 加一;
                                    bossMonster.setOrdinaryAttack(bossMonster.getOrdinaryAttack()+1);
                                    // 用户减血
                                    user.setCurrHp(user.getCurrHp() - subHp);
                                    GameMsg.BossAttkUserResult attkCmd = GameMsg.BossAttkUserResult.newBuilder()
                                            .setSubUserHp(subHp)
                                            .build();
                                    user.getCtx().writeAndFlush(attkCmd);
                                }


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
