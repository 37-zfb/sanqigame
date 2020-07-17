package codoc;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsgRecognizer;

/**
 * @author 张丰博
 * 自定义编码器
 */
@Slf4j
public class GameMsgEncoder extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (ctx == null || msg == null) {
            return;
        }

        if (!(msg instanceof GeneratedMessageV3)) {
            super.write(ctx, msg, promise);
            return;
        }

        // 获得消息类型
        Class<?> msgClass = msg.getClass();
        // 根据类型获得消息编码
        int msgCode = GameMsgRecognizer.getMsgCodeByMsgClass(msgClass);
        if (msgCode < 0) {
            log.error("无法识别的消息类型:{}", msgClass);
            return;
        }

        byte[] msgBody = ((GeneratedMessageV3) msg).toByteArray();


        ByteBuf byteBuf = ctx.alloc().buffer();
        //写出消息长度
        byteBuf.writeShort((short) msgBody.length);
        //写出消息编码
        byteBuf.writeShort((short) msgCode);
        byteBuf.writeBytes(msgBody);

        WebSocketFrame frame = new BinaryWebSocketFrame(byteBuf);

        super.write(ctx,frame,promise);
        System.out.println("GameMsgEncoder 。。。。");
    }
}
