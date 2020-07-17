package server.entity;

import client.model.Role;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 张丰博
 */
@Slf4j
public class RoleManager {
    private static final Map<Integer, Role> ROLE_MAP = new HashMap<>();

    private RoleManager() {
    }

    /**
     *  添加用户角色
     * @param role
     */
    public static void addRole(Role role) {
        if (role != null){
            ROLE_MAP.put(role.getId(),role);
        }
    }

    /**
     *  根据 用户id 移除用户
     * @param userId
     */
    public static void removeRole(Integer userId){
        ROLE_MAP.remove(userId);
    }

    /**
     *
     * @return  用户集合
     */
    public static Collection<Role> listRole(){
        return ROLE_MAP.values();
    }

    /**
     *  通过用户id获得用户角色信息
     * @param userId
     * @return
     */
    public static Role getRoleById(Integer userId){
        return ROLE_MAP.get(userId);
    }

}
