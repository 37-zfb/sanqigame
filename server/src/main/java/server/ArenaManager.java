package server;

import msg.GameMsg;
import server.model.User;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 竞技场
 */
public final class ArenaManager {
    /**
     * 保存竞技场中的用户
     */
    private static final Map<Integer, User> ARENA_MAP = new ConcurrentHashMap<>();

    private ArenaManager() {
    }

    /**
     * 添加用户
     *
     * @param user
     */
    public static void addUser(User user) {
        if (user == null) {
            return;
        }
        broadcastUserEnterArena(user);

        ARENA_MAP.put(user.getUserId(), user);
    }

    /**
     * 移除用户
     *
     * @param user
     */
    public static void removeUser(User user) {
        if (user == null) {
            return;
        }
        // 这里也要广播用户离场
        ARENA_MAP.remove(user.getUserId());
        broadcastUserQuitArena(user);
    }

    /**
     * 获得竞技场玩家
     *
     * @return
     */
    public static Collection<User> getArenaUser() {
        return ARENA_MAP.values();
    }

    /**
     * 广播用户进入竞技场
     *
     * @param user
     */
    private static void broadcastUserEnterArena(User user) {
        GameMsg.UserInfo.Builder userInfo = GameMsg.UserInfo.newBuilder()
                .setUserId(user.getUserId())
                .setUserName(user.getUserName());
        GameMsg.UserEnterArenaResult userEnterArenaResult = GameMsg.UserEnterArenaResult.newBuilder().addUserInfo(userInfo).build();

        for (User u : ARENA_MAP.values()) {
            u.getCtx().writeAndFlush(userEnterArenaResult);
        }
    }

    /**
     * 广播用户离开竞技场
     *
     * @param user
     */
    private static void broadcastUserQuitArena(User user) {
        GameMsg.UserInfo.Builder userInfo = GameMsg.UserInfo.newBuilder()
                .setUserId(user.getUserId())
                .setUserName(user.getUserName());
        GameMsg.UserQuitArenaResult userQuitArenaResult = GameMsg.UserQuitArenaResult.newBuilder()
                .setUserInfo(userInfo)
                .build();
        for (User u : ARENA_MAP.values()) {
            u.getCtx().writeAndFlush(userQuitArenaResult);
        }
    }

    public static boolean isExist(User user) {
        if (user == null) {
            return true;
        }
        return ARENA_MAP.get(user) != null;
    }

    public static User getUserById(Integer userId) {
        if (userId == null) {
            return null;
        }
        return ARENA_MAP.get(userId);
    }
}
