package server.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.model.scene.Monster;
import server.model.scene.Scene;
import server.scene.GameData;
import server.PublicMethod;
import server.model.User;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 *  开启 怪 自动攻击
 */
@Component
@Slf4j
public class MonsterStartAttkUserHandler implements ICmdHandler<GameMsg.MonsterStartAttkUser> {

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.MonsterStartAttkUser cmd) {

        MyUtil.checkIsNull(ctx,cmd);
        User user = PublicMethod.getInstance().getUser(ctx);
        Scene scene = GameData.getInstance().getSceneMap().get(user.getCurSceneId());

        GameMsg.NoticeUserAttked.Builder noticeUser = GameMsg.NoticeUserAttked.newBuilder();
        // 获得当前场景中的怪
        Map<Integer, Monster> monsterMap = scene.getMonsterMap();
        if (monsterMap.size() != 0) {
            // 添加定时任务
            for (Monster monster : monsterMap.values()) {
                if (monster.isDie()){
                    continue;
                }
//                RunnableScheduledFuture runnableScheduledFuture = MonsterAttakTimer.getInstance().monsterNormalAttk(user, monster,ctx);
                // 一个怪攻击一个人，添加定时任务到 monster 对象中。
//                monster.getTimerMap().put(user.getUserId(),runnableScheduledFuture);
                noticeUser.addMonsterId(monster.getId());
            }
        }

        // 通知协议，通知用户受到了 怪 的攻击
        GameMsg.NoticeUserAttked noticeUserAttked = noticeUser.build();
        ctx.channel().writeAndFlush(noticeUserAttked);
    }

}
