package server.cmdhandler.arena;

import constant.ProfessionConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import util.MyUtil;

/**
 * @author 张丰博
 * 竞技结束后，整理
 */
@Component
@Slf4j
public class SortOutArenaCmdHandler implements ICmdHandler<GameMsg.SortOutArenaCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.SortOutArenaCmd sortOutArenaCmd) {
        MyUtil.checkIsNull(ctx, sortOutArenaCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        user.setCurrMp(ProfessionConst.MP);
        user.setCurrHp(ProfessionConst.HP);

        GameMsg.SortOutArenaResult sortOutArenaResult = GameMsg.SortOutArenaResult.newBuilder().build();
        ctx.writeAndFlush(sortOutArenaResult);

    }
}
