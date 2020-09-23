package server.cmdhandler.deal;

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
 * 交易建立失败，把自身状态初始化
 */
@Component
@Slf4j
public class DealFailCmdHandler implements ICmdHandler<GameMsg.DealFailCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.DealFailCmd dealFailCmd) {

        MyUtil.checkIsNull(ctx, dealFailCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        user.setDeal(null);

        GameMsg.DealFailResult dealFailResult = GameMsg.DealFailResult.newBuilder().build();
        ctx.writeAndFlush(dealFailResult);
    }
}
