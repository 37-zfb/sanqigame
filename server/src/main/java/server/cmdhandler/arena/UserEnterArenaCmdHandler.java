package server.cmdhandler.arena;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.ArenaManager;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayArena;
import server.model.User;
import util.MyUtil;

import java.util.Collection;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public class UserEnterArenaCmdHandler implements ICmdHandler<GameMsg.UserEnterArenaCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserEnterArenaCmd userEnterArenaCmd) {
        MyUtil.checkIsNull(ctx, userEnterArenaCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        GameMsg.UserEnterArenaResult.Builder newBuilder = GameMsg.UserEnterArenaResult.newBuilder();
        Collection<User> arenaUser = ArenaManager.getArenaUser();
        for (User u : arenaUser) {
            GameMsg.UserInfo.Builder userInfo = GameMsg.UserInfo.newBuilder()
                    .setUserId(u.getUserId())
                    .setUserName(u.getUserName());
            newBuilder.addUserInfo(userInfo);
        }

        // 加入竞技场对象
        user.setPlayArena(new PlayArena());
        // 加入竞技场
        ArenaManager.addUser(user);
        log.info("玩家: {},进入了竞技场;", user.getUserName());
        GameMsg.UserEnterArenaResult userEnterArenaResult = newBuilder.build();
        ctx.writeAndFlush(userEnterArenaResult);
    }
}