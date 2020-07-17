package server;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.Locale;

/**
 * @author 张丰博
 */
public class GameServerHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        // 添加channel到 ChannelGroup
        Broadcaster.addChannel(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
//
//        System.out.println(msg.getClass());
//        System.out.println("handler,channelRead0");

        if (msg instanceof GeneratedMessageV3) {
            MainThreadProcessor.getInstance().process(ctx, (GeneratedMessageV3) msg);
        }

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        Broadcaster.removeChannel(ctx.channel());

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause.getMessage());
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
