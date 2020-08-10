package client;

import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import util.CustomizeThreadFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 * <p>
 * 怪 攻击 角色
 */
@Slf4j
public class MonsterAttack {

    private final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(
            1,
            new CustomizeThreadFactory("调度")
    );


    private static final MonsterAttack MONSTER_ATTACK = new MonsterAttack();

    public static MonsterAttack getInstance() {
        return MONSTER_ATTACK;
    }

    /**
     * 普通攻击
     *
     * @param role 用户
     * @param ctx
     * @return
     */
    public RunnableScheduledFuture monsterNormalAttk(Role role, ChannelHandlerContext ctx) {
        if (role == null) {
            return null;
        }
        RunnableScheduledFuture<?> scheduledFuture =
                (RunnableScheduledFuture<?>) scheduledThreadPool.scheduleAtFixedRate(() -> {
                    log.info("怪攻击");
                    GameMsg.AttkCmd attkCmd = GameMsg.AttkCmd.newBuilder().setTargetUserId(role.getId()).build();
                    ctx.writeAndFlush(attkCmd);

                }, 1000, 1000, TimeUnit.MILLISECONDS);
        return scheduledFuture;
    }

    /**
     * 移除指定的任务
     *
     * @param scheduledFuture 任务对象
     */
    public void removeTask(RunnableScheduledFuture<?> scheduledFuture) {
//        scheduledThreadPool.remove(scheduledFuture);
    }


    public void shutdownNow() {
        scheduledThreadPool.shutdownNow();
    }

}
