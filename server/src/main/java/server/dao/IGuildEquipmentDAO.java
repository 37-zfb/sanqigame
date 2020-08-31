package server.dao;


import entity.db.DbGuildEquipment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author 张丰博
 */
@Repository
public interface IGuildEquipmentDAO {

    /**
     *  批量添加，
     * @param addGuildEquipmentList
     */
    void insertEquipmentBatch(@Param("addGuildEquipmentList") List<DbGuildEquipment> addGuildEquipmentList);

    /**
     * 批量删除
     * @param deleteGuildEquipmentList
     */
    void deleteEquipmentBatch(@Param("deleteGuildEquipmentList") List<DbGuildEquipment> deleteGuildEquipmentList);

    /**
     * 获取公会仓库中的装备
     * @param guildId
     * @return
     */
    List<DbGuildEquipment> selectGuildEquipment(@Param("guildId") Integer guildId);
}
