package server.timer;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import model.scene.Monster;
import msg.GameMsg;
import server.model.User;
import util.CustomizeThreadFactory;

import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 */
@Slf4j
public class UserAutomaticSubHpTimer {

    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("用户出血")
    );

    private static final UserAutomaticSubHpTimer USER_AUTOMATIC_SUB_HP_TIMER = new UserAutomaticSubHpTimer();

    public static UserAutomaticSubHpTimer getInstance() {
        return USER_AUTOMATIC_SUB_HP_TIMER;
    }


    public void userSubHpAuto(User user,Integer subHpNumber) {
        if (user == null) {
            return;
        }

        RunnableScheduledFuture<?> scheduledFuture =
                (RunnableScheduledFuture<?>) scheduledThreadPool
                        .scheduleAtFixedRate(() -> {
                            // 防止多线程执行时，减血超减
                            synchronized (user.getHpMonitor()) {
                                //
                                user.calCurrHp();
                                if (user.getSubHpNumber() < subHpNumber && user.getCurrHp() > 20) {

                                    log.info("用户 {}:血量 {} -20", user.getUserName(), user.getCurrHp());

                                    user.setCurrHp(user.getCurrHp() - 20);
                                    user.setSubHpNumber(user.getSubHpNumber()+1);
                                } else if (user.getSubHpNumber() < subHpNumber && user.getCurrHp() <= 20) {

                                    log.info("用户 {} 自动掉血而死", user.getUserName());

                                    // 怪死了，设置0
//                                    monster.getDropHpNumber().set(0);
                                    user.setCurrHp(0);
                                    // 取消定时任务
                                    user.getSubHpTask().cancel(true);
                                    user.setSubHpTask(null);
                                    // 发送死亡消息

//                                    GameMsg.MonsterDropHpAutoDie autoDie = GameMsg.MonsterDropHpAutoDie.newBuilder()
//                                            .setMonsterId(monster.getId())
//                                            .build();
                                    GameMsg.DieResult dieResult = GameMsg.DieResult.newBuilder()
                                            .setTargetUserId(user.getUserId())
                                            .build();
                                    user.getCtx().channel().writeAndFlush(dieResult);
                                } else {
                                    log.info("{} 停止出血状态;", user.getUserName());
                                    // 掉血次数用完，设置0
                                    user.setSubHpNumber(0);
                                    // 取消定时任务
                                    user.getSubHpTask().cancel(true);
                                    user.setSubHpTask(null);
                                }

                            }

                        }, 1000, 1000, TimeUnit.MILLISECONDS);

        user.setSubHpTask(scheduledFuture);
    }




}
