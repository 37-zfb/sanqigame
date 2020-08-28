package server.service;

import entity.db.GuildEntity;
import entity.db.GuildMemberEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.dao.IGuildEntityDAO;
import server.dao.IGuildMemberDAO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author 张丰博
 */
@Service
public class GuildService {

    @Autowired
    private IGuildEntityDAO guildEntityDAO;

    @Autowired
    private IGuildMemberDAO guildMemberDAO;

    /**
     * 批量增加 公会
     *
     * @param addGuildEntityCollection
     */
    @Transactional(rollbackFor = Exception.class)
    public void addGuildInfoBatch(Collection<GuildEntity> addGuildEntityCollection) {
        if (addGuildEntityCollection == null) {
            return;
        }
        guildEntityDAO.insertGuildBatch(addGuildEntityCollection);
    }

    /**
     * 批量 增加 公会成员
     *
     * @param addGuildMemberEntityCollection
     */
    public void addGuildMemberBatch(Collection<GuildMemberEntity> addGuildMemberEntityCollection) {
        if (addGuildMemberEntityCollection == null) {
            return;
        }
        guildMemberDAO.insertGuildMemberBatch(addGuildMemberEntityCollection);
    }

    /**
     * 查询所有公会信息
     *
     * @return
     */
    public List<GuildEntity> listGuildInfo() {
        return guildEntityDAO.selectGuildEntity();
    }

    /**
     * 查询所有公会成员
     *
     * @return
     */
    public List<GuildMemberEntity> listGuildMember() {
        return guildMemberDAO.selectGuildMember();
    }

    /**
     * 删除解散的公会
     *
     * @param guildEntityCollection
     */
    public void deleteGuildEntityBatch(Collection<GuildEntity> guildEntityCollection) {
        if (guildEntityCollection != null) {
            guildEntityDAO.deleteGuildEntityBatch(guildEntityCollection);
        }
    }

    /**
     * 当公会解散时，删除公会成员信息
     *
     * @param guildMemberEntityCollection
     */
    public void deleteGuildMemberEntityBatch(Collection<GuildMemberEntity> guildMemberEntityCollection) {
        if (guildMemberEntityCollection != null) {
            guildMemberDAO.deleteGuildMemberBatch(guildMemberEntityCollection);
        }
    }
}
