package client;

import client.cmd.ICmd;
import client.cmd.ResultHandlerFactory;
import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 张丰博
 */
@Slf4j
public class GameClientHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    public GameClientHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    /**
     * 当前handler  被添加到ChannelPipeline时，new出握手的结果的实例，以备将来使用
     *
     * @param ctx
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    /**
     * 建立通道，进行握手
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        System.out.println("client:ChannelHandlerContext" + msg.getClass());
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                System.out.println("WebSocket Client connected!");
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                System.out.println("WebSocket Client failed to connect");
                handshakeFuture.setFailure(e);
            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }
//        Login login = new Login();
//        User user = login.login();

        if (msg instanceof GeneratedMessageV3) {
            process(ctx, (GeneratedMessageV3) msg);
        }

    }


    private void process(ChannelHandlerContext ctx, GeneratedMessageV3 msg) {
        if (null == ctx ||
                null == msg) {
            return;
        }

        // 获取消息类
        Class<?> msgClazz = msg.getClass();

        log.info(
                "收到服务端消息, msgClazz = {}",
                msgClazz.getName()
        );

        // 获取指令处理器
        ICmd<? extends GeneratedMessageV3>
                cmdHandler = ResultHandlerFactory.getHandlerByClazz(msgClazz);

        if (null == cmdHandler) {
            log.error(
                    "未找到相对应的指令处理器, msgClazz = {}",
                    msgClazz.getName()
            );
            return;
        }

        try {
            // 处理指令
            cmdHandler.cmd(ctx, cast(msg));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
    /**
     * 转义消息对象
     * @param msg 消息对象
     * @param <TCmd>
     * @return
     */
    private <TCmd extends GeneratedMessageV3> TCmd cast(GeneratedMessageV3 msg) {
        if (msg == null || !(msg instanceof GeneratedMessageV3)) {
            return null;
        }
        return (TCmd) msg;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

}
