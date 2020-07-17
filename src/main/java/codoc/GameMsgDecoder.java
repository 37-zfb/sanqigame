package codoc;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsgRecognizer;

/**
 * @author 张丰博
 */
@Slf4j
public class GameMsgDecoder extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("解码中。。。。");
        log.info("解码中。。。");
        if (!(msg instanceof BinaryWebSocketFrame)) {
            super.channelRead(ctx,msg);
            return;
        }
        System.out.println("BinaryWebSocketFrame");

        BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
        ByteBuf content = frame.content();

        // 消息长度
        short msgBodyLength = content.readShort();
        // 消息编号
        short msgCode = content.readShort();

        // 获得消息构建者
        Message.Builder builder = GameMsgRecognizer.getMsgBuilderByMsgCode(msgCode);
        if (builder == null){
            log.info("无法识别的消息类型:{}",msgCode);
            return;
        }

        byte[] msgBody = new byte[msgBodyLength];
        content.readBytes(msgBody);

        builder.clear();
        Message message = builder.mergeFrom(msgBody).build();

        if (message != null){
            ctx.fireChannelRead(message);
        }
    }
}
