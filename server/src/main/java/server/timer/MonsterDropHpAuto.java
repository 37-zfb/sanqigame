package server.timer;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import server.model.scene.Monster;
import util.CustomizeThreadFactory;

import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 */
@Slf4j
public class MonsterDropHpAuto {

    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("怪出血")
    );

    private static final MonsterDropHpAuto MONSTER_DROP_HP_AUTO = new MonsterDropHpAuto();

    public static MonsterDropHpAuto getInstance() {
        return MONSTER_DROP_HP_AUTO;
    }


    public void monsterDropHpAuto(Monster monster, ChannelHandlerContext ctx) {
        if (monster == null) {
            return;
        }

        RunnableScheduledFuture<?> scheduledFuture =
                (RunnableScheduledFuture<?>) scheduledThreadPool
                        .scheduleAtFixedRate(() -> {
                            // 防止多线程执行时，减血超减
                            synchronized (monster.getSubHpMonitor()) {
                                // 掉血掉两次
                                if (monster.getDropHpNumber().get() < 2 && monster.getHp() > 20) {

                                    log.info("{}:血量 {} -20",monster.getName(),monster.getHp());

                                    monster.setHp(monster.getHp() - 20);
                                    monster.getDropHpNumber().getAndIncrement();
                                } else if (monster.getDropHpNumber().get() < 2 && monster.getHp() <= 20) {

                                    log.info("{}自动掉血而死",monster.getName(),monster.getHp());

                                    // 怪死了，设置0
                                    monster.getDropHpNumber().set(0);

                                    monster.setHp(0);
                                    // 取消定时任务
                                    monster.getScheduledFuture().cancel(true);
                                    monster.setScheduledFuture(null);
                                    // 发送死亡消息
                                    GameMsg.MonsterDropHpAutoDie autoDie = GameMsg.MonsterDropHpAutoDie.newBuilder()
                                            .setMonsterId(monster.getId())
                                            .build();
                                    ctx.channel().writeAndFlush(autoDie);
                                } else {
                                    log.info("{} 停止出血状态;",monster.getName());
                                    // 掉血次数用完，设置0
                                    monster.getDropHpNumber().set(0);
                                    // 取消定时任务
                                    monster.getScheduledFuture().cancel(true);
                                    monster.setScheduledFuture(null);
                                }

                            }

                        }, 1000, 1000, TimeUnit.MILLISECONDS);

        monster.setScheduledFuture(scheduledFuture);
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
