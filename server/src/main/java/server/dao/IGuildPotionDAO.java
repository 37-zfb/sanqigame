package server.dao;


import entity.db.DbGuildPotion;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author 张丰博
 */
@Repository
public interface IGuildPotionDAO {

    /**
     * 批量添加药剂
     * @param addGuildPotionList
     */
    void insertPotionBatch(@Param("addGuildPotionList") List<DbGuildPotion> addGuildPotionList);

    /**
     * 批量删除药剂
     * @param deleteGuildPotionList
     */
    void deletePotionBatch(@Param("deleteGuildPotionList") List<DbGuildPotion> deleteGuildPotionList);

    /**
     * 批量修改药剂
     * @param modifyGuildPotionList
     */
    void updatePotionBatch(@Param("modifyGuildPotionList") List<DbGuildPotion> modifyGuildPotionList);

    /**
     * 通过公会id获取公会仓库中的药剂
     * @param guildId
     * @return
     */
    List<DbGuildPotion> selectGuildPotion(@Param("guildId") Integer guildId);
}
