package server;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import scene.GameData;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author 张丰博
 */
public final class Broadcast {

    /**
     * k:场景id <==> v:ChannelGroup
     */
    private static final Map<Integer, ChannelGroup> CHANNEL_GROUP_MAP = new HashMap<>();

    private Broadcast() {
    }

    /**
     * 初始化CHANNEL_GROUP_MAP
     */
    public static void init() {
        Set<Integer> keySet = GameData.getInstance().getSceneMap().keySet();
        for (Integer integer : keySet) {
            CHANNEL_GROUP_MAP.put(integer, new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
        }
    }

    /**
     * 添加管道到当前场景
     *
     * @param sceneId    场景id
     * @param newChannel 管道
     */
    public static void addChannel(Integer sceneId, Channel newChannel) {
        if (sceneId == null || newChannel == null) {
            return;
        }
        CHANNEL_GROUP_MAP.get(sceneId).add(newChannel);
    }

    /**
     * 移除指定场景的管道
     *
     * @param sceneId 场景id
     * @param channel 管道
     */
    public static void removeChannel(Integer sceneId, Channel channel) {
        if (channel != null && sceneId != null) {
            CHANNEL_GROUP_MAP.get(sceneId).remove(channel);
        }
    }


    /**
     * 单个场景广播消息
     *
     * @param sceneId 当前场景id
     * @param msg     消息
     */
    public static void broadcast(Integer sceneId, Object msg) {
        if (sceneId != null) {
            CHANNEL_GROUP_MAP.get(sceneId).writeAndFlush(msg);
        }
    }

    /**
     * 全服广播消息
     *
     * @param msg 消息
     */
    public static void allBroadcast(Object msg) {
        if (msg == null) {
            return;
        }
        for (ChannelGroup channelGroup : CHANNEL_GROUP_MAP.values()) {
            channelGroup.writeAndFlush(msg);
        }

    }

}
