package server;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import entity.db.CurrUserStateEntity;
import server.model.User;
import server.model.UserManager;
import server.service.UserService;

/**
 * @author 张丰博
 */
@Slf4j
public class GameServerHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("消息类型: "+msg.getClass());

        if (msg instanceof GeneratedMessageV3) {
            MainThreadProcessor.getInstance().process(ctx, (GeneratedMessageV3) msg);
        }

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);

        // 拿到用户 Id
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        if (null == userId) {
            return;
        }

        log.info("用户离线, userId = {}", userId);

        ApplicationContext context = GameServer.APPLICATION_CONTEXT;
        UserService userService = context.getBean(UserService.class);

        User user = UserManager.getUserById(userId);
        CurrUserStateEntity userStateEntity = new CurrUserStateEntity();
        user.calCurrHp();
        userStateEntity.setCurrHp(user.getCurrHp());
        // 计算当前mp
        user.calCurrMp();
        userStateEntity.setCurrMp(user.getCurrMp());
        userStateEntity.setCurrSceneId(user.getCurSceneId());
        userStateEntity.setBaseDamage(user.getBaseDamage());
        userStateEntity.setBaseDefense(user.getBaseDefense());
        userStateEntity.setUserId(userId);

        // 保存用户所在地
        userService.modifyUserState(userStateEntity);

        // 移除管道
        Broadcast.removeChannel(user.getCurSceneId(),ctx.channel());

        // 移除用户
        UserManager.removeUser(userId);

//        // 广播用户离场的消息
//        GameMsg.UserQuitResult.Builder resultBuilder = GameMsg.UserQuitResult.newBuilder();
//        resultBuilder.setQuitUserId(userId);
//
//        GameMsg.UserQuitResult newResult = resultBuilder.build();
//        Broadcaster.broadcast(newResult);
//
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause.getMessage());
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
