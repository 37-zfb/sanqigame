package client.cmd;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author 张丰博
 */
public interface ICmd<TCmd extends GeneratedMessageV3> {
    /**
     * 业务处理方法
     * @param ctx 信道上下文
     * @param cmd 对应的消息类型
     */
    void cmd(ChannelHandlerContext ctx, TCmd cmd);
}
