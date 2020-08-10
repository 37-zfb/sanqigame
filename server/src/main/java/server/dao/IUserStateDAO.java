package server.dao;

import org.springframework.stereotype.Repository;
import entity.db.CurrUserStateEntity;

/**
 * @author 张丰博
 */
@Repository
public interface IUserStateDAO {

    /**
     *  添加用户状态
     * @param currUserState 用户当前状态
     */
    void insertUserSate(CurrUserStateEntity currUserState);

    /**
     *  通过userId获取用户当前状态
     * @param userId 用户id
     * @return CurrUserStateEntity 对象
     */
    CurrUserStateEntity selectUserStateByUserId(Integer userId);


    /**
     *  修改当前用户状态
     * @param currUserState 用户状态
     */
    void updateUserState(CurrUserStateEntity currUserState);
}
