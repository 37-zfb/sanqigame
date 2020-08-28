package server.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import entity.db.CurrUserStateEntity;

import javax.annotation.PostConstruct;
import java.util.Set;

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


    /**
     *  通过 用户id更新当前 金币
     * @param userId
     * @param money
     */
    void updateUserMoney(@Param("userId") Integer userId,@Param("money") Integer money);

    /**
     * 改变用户公会状态
     * @param guildStateSet
     * @param state
     */
    void updateGuildState(@Param("guildStateSet") Set<Integer> guildStateSet,@Param("state") Integer state);
}
