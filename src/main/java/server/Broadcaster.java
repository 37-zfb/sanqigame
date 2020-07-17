package server;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author 张丰博
 */
public class Broadcaster {

    private static final ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private Broadcaster() {
    }

    /**
     * 添加新的客户端 channel
     *
     * @param newChannel
     */
    public static void addChannel(Channel newChannel) {
        if (newChannel != null) {
            CHANNEL_GROUP.add(newChannel);
        }
    }

    public static void removeChannel(Channel channel) {
        if (channel != null) {
            CHANNEL_GROUP.remove(channel);
        }
    }

    /**
     * 广播消息
     * @param msg 消息对象
     */
    public static void broadcast(Object msg) {
        if (null == msg) {
            return;
        }
        CHANNEL_GROUP.writeAndFlush(msg);
    }

}
