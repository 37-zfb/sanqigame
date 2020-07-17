package server.dao;

import org.apache.ibatis.annotations.Param;
import server.entity.UserEntity;

/**
 * @author 张丰博
 */
public interface UserDAO {
    /**
     * 根据用户名称获取用户实体
     *
     * @param userName 用户名称
     * @return 用户实体
     */
    UserEntity getUserByName(@Param("userName") String userName);

    /**
     * 添加用户实体
     *
     * @param newUserEntity 用户实体
     */
    int insertInto(UserEntity newUserEntity);
}
