package server.timer;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import server.model.User;
import util.CustomizeThreadFactory;

import java.util.concurrent.*;

/**
 * @author 张丰博
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
     * @param user 当前用户
     * @param ctx
     * @return
     */
    public RunnableScheduledFuture monsterNormalAttk(User user, String monsterName, ChannelHandlerContext ctx) {
        if (user == null || ctx == null) {
            return null;
        }

        RunnableScheduledFuture<?> scheduledFuture =
                (RunnableScheduledFuture<?>) scheduledThreadPool
                        .scheduleAtFixedRate(() -> {

                            // 防止多线程执行时，减血超减
                            synchronized (MonsterAttakTimer.class){
                                // 用户减血
                                if (user.getCurrHp() <= 0 || (user.getCurrHp() - 2) <= 0) {
                                    user.setCurrHp(Math.abs(user.getCurrHp() - 2) + user.getCurrHp() - 2);
                                    // 发送死亡消息
                                    GameMsg.DieResult dieResult = GameMsg.DieResult.newBuilder()
                                            .setTargetUserId(user.getUserId())
                                            .build();
                                    ctx.channel().writeAndFlush(dieResult);
                                } else {
                                    user.setCurrHp(user.getCurrHp() - 2);
                                    GameMsg.AttkCmd attkCmd = GameMsg.AttkCmd.newBuilder()
                                            .setTargetUserId(user.getUserId())
                                            .setMonsterName(monsterName)
                                            .build();
                                    ctx.channel().writeAndFlush(attkCmd);
                                }
                            }


                        }, 3000, 3000, TimeUnit.MILLISECONDS);

        return scheduledFuture;
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
