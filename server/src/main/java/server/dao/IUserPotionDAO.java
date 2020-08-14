package server.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import entity.db.UserPotionEntity;

import java.util.List;

/**
 * @author 张丰博
 */
@Repository
public interface IUserPotionDAO {

    /**
     *  添加 玩家获得的药剂
     * @param userPotionEntity
     * @return
     */
    int insertPotion(UserPotionEntity userPotionEntity);


    /**
     *  更改药剂数量
     * @param entity
     */
    void updatePotionNumber(UserPotionEntity entity);

    /**
     *  通过用户id，药剂id查询药剂
     * @param userId
     * @param potionId
     * @return
     */
    UserPotionEntity selectPotionByUserIdAndPotionId(@Param("userId") int userId,@Param("propsId") int potionId);

    /**
     *  根据玩家id查询药剂
     * @param userId
     * @return
     */
    List<UserPotionEntity> selectPotionByUserId(int userId);

}
