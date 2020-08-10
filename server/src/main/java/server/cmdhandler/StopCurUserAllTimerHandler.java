package server.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import model.scene.Monster;
import model.scene.Scene;
import scene.GameData;
import server.model.User;
import server.model.UserManager;
import server.timer.MonsterAttakTimer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.RunnableScheduledFuture;

/**
 * @author 张丰博
 */
@Slf4j
@Component
public class StopCurUserAllTimerHandler implements ICmdHandler<GameMsg.StopCurUserAllTimer> {

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.StopCurUserAllTimer cmd) {

        if (ctx == null || cmd == null){
            return;
        }
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        if (userId == null){
            return;
        }

        User user = UserManager.getUserById(userId);
        if (user == null){
            return;
        }

        log.info("用户: {} 已阵亡!",user.getUserName());

        Scene scene = GameData.getInstance().getSceneMap().get(user.getCurSceneId());

        // 取消当前用户的所有定时任务
        Map<Integer, Monster> monsterMap = scene.getMonsterMap();
        for (Monster monster : monsterMap.values()) {
            RunnableScheduledFuture runnableScheduledFuture = monster.getTimerMap().get(user);
            if (runnableScheduledFuture != null){
                MonsterAttakTimer.getInstance().removeTask(runnableScheduledFuture);
            }
        }


    }

}
