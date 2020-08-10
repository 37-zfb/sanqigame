package server.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import entity.db.UserEquipmentEntity;

import java.util.List;

/**
 * @author 张丰博
 */
@Repository
public interface IUserEquipmentDAO {

    /**
     *  添加 玩家获得的装备
     * @param userEquipmentEntity
     * @return
     */
    int insertEquipment(UserEquipmentEntity userEquipmentEntity);


    /**
     *  更新装备状态
     * @param id
     * @param state
     */
    void updateEquipmentState(@Param("id") int id,@Param("state") int state,@Param("location") int location);

    /**
     *  查询
     * @param userId
     * @param state
     * @return
     */
    List<UserEquipmentEntity> selectEquipmentByUserIdAndState(@Param("userId") Integer userId,@Param("state") Integer state);

    /**
     *  根据id，修改此装备的耐久度
     * @param id
     * @param durability
     */
    void updateEquipmentDurability(@Param("id") Integer id,@Param("durability") Integer durability);
}
