package server.dao;

import entity.db.UserBuyGoodsLimitEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @author 张丰博
 */
@Repository
public interface IUserGoodsLimitDAO {



    /**
     *  通关userid、日期查询
     * @param userId
     * @param date
     * @return
     */
    List<UserBuyGoodsLimitEntity> selectEntitiesByUserIdAndDate(@Param("userId") Integer userId,@Param("date") String date);

    /**
     *  添加数据
     * @param goodsLimitEntity
     */
    void insertEntity(UserBuyGoodsLimitEntity goodsLimitEntity);

    /**
     *  更新已购买的个数
     * @param goodsLimitEntity
     */
    void updateLimitNumber(UserBuyGoodsLimitEntity goodsLimitEntity);
}
