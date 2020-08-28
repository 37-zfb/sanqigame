package server.model;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 张丰博
 */
@Slf4j
public class UserManager {

    private static final Map<Integer, User> USER_MAP = new ConcurrentHashMap<>();

    private UserManager() {
    }

    /**
     *  添加用户角色
     * @param user
     */
    public static void addUser(User user) {
        if (user != null){
            USER_MAP.put(user.getUserId(),user);
        }
    }

    /**
     *  根据 用户id 移除用户
     * @param userId
     */
    public static void removeUser(Integer userId){
        USER_MAP.remove(userId);
    }

    /**
     *
     * @return  用户集合
     */
    public static Collection<User> listUser(){
        return USER_MAP.values();
    }

    /**
     *  通过用户id获得用户角色信息
     * @param userId
     * @return
     */
    public static User getUserById(Integer userId){
        return USER_MAP.get(userId);
    }

}
