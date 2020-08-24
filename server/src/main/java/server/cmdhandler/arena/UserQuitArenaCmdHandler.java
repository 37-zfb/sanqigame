package server.cmdhandler.arena;

import constant.ProfessionConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.ArenaManager;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import util.MyUtil;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public class UserQuitArenaCmdHandler implements ICmdHandler<GameMsg.UserQuitArenaCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserQuitArenaCmd userQuitArenaCmd) {
        MyUtil.checkIsNull(ctx, userQuitArenaCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        user.setPlayArena(null);
        // 离开竞技场
        ArenaManager.removeUser(user);
        user.setCurrHp(ProfessionConst.HP);
        user.setCurrMp(ProfessionConst.MP);
        log.info("玩家: {},离开了竞技场;", user.getUserName());
        GameMsg.UserInfo.Builder userInfo = GameMsg.UserInfo.newBuilder()
                .setUserId(user.getUserId())
                .setUserName(user.getUserName());
        GameMsg.UserQuitArenaResult userQuitArenaResult = GameMsg.UserQuitArenaResult.newBuilder()
                .setUserInfo(userInfo)
                .build();
        ctx.writeAndFlush(userQuitArenaResult);
    }
}
