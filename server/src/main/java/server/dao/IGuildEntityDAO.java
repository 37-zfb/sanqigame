package server.dao;

import entity.db.GuildEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author 张丰博
 */
@Repository
public interface IGuildEntityDAO {
    /**
     * 批量添加公会信息
     * @param addGuildEntityCollection
     */
    void insertGuildBatch(@Param("addGuildEntityCollection") Collection<GuildEntity> addGuildEntityCollection);

    /**
     *  查询所有的公会信息
     * @return
     */
    List<GuildEntity> selectGuildEntity();

    /**
     * 删除公会信息
     * @param guildEntityCollection
     */
    void deleteGuildEntityBatch(@Param("guildEntityCollection") Collection<GuildEntity> guildEntityCollection);
}
