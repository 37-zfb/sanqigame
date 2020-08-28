package server.dao;

import entity.db.GuildMemberEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author 张丰博
 */
@Repository
public interface IGuildMemberDAO {
    /**
     *  添加公会成员
     * @param addGuildMemberEntityCollection
     */
    void insertGuildMemberBatch(@Param("addGuildMemberEntityCollection") Collection<GuildMemberEntity> addGuildMemberEntityCollection);

    /**
     *  查询所有公会成员
     * @return
     */
    List<GuildMemberEntity> selectGuildMember();

    /**
     * 公会解散时，删除公会成员
     * @param guildMemberEntityCollection
     */
    void deleteGuildMemberBatch(@Param("guildMemberEntityCollection") Collection<GuildMemberEntity> guildMemberEntityCollection);
}
