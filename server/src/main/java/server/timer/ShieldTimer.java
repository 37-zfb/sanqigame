package server.timer;

import lombok.extern.slf4j.Slf4j;
import server.GameServer;
import server.cmdhandler.task.listener.TaskUtil;
import server.model.User;
import util.CustomizeThreadFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 */
@Slf4j
public final class ShieldTimer {

    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("护盾线程")
    );
    private final TaskUtil taskUtil = GameServer.APPLICATION_CONTEXT.getBean(TaskUtil.class);

    private ShieldTimer(){}

    private static final ShieldTimer SHIELD_TIMER = new ShieldTimer();

    public static ShieldTimer getInstance() {
        return SHIELD_TIMER;
    }

    public void cancelShield(User user,Integer delayTime){
        if (user == null){
            return;
        }

        scheduledThreadPool.schedule(()->{
            synchronized (user.getSHIELD_MONITOR()){
                user.setShieldValue(0);
                log.info("用户 {} 护盾时间到;", user.getUserName());
            }
        }, delayTime, TimeUnit.SECONDS);

    }

}
