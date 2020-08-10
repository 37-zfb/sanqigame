package server.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import entity.db.UserEntity;

/**
 * @author 张丰博
 */
@Repository
public interface IUserDAO {
    /**
     * 根据用户名称获取用户实体
     *
     * @param userName 用户名称
     * @return 用户实体
     */
    UserEntity selectUserByName(@Param("userName") String userName);

    /**
     *  添加用户实体
     * @param newUserEntity 用户对象
     * @return 用户id
     */
    int insertInto(UserEntity newUserEntity);
}
