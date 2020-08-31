package server.service;

import entity.db.DbGuildEquipment;
import entity.db.DbGuildPotion;
import entity.db.GuildEntity;
import entity.db.GuildMemberEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.dao.IGuildEntityDAO;
import server.dao.IGuildEquipmentDAO;
import server.dao.IGuildMemberDAO;
import server.dao.IGuildPotionDAO;

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

    @Autowired
    private IGuildEquipmentDAO guildEquipmentDAO;

    @Autowired
    private IGuildPotionDAO guildPotionDAO;

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

    /**
     * 获得公会成员
     *
     * @param userId
     * @return
     */
    public GuildMemberEntity findGuildMemberById(int userId) {
        return guildMemberDAO.selectGuildMemberByUserId(userId);
    }

    /**
     * 添加公会道具
     *
     * @param addGuildEquipmentList
     */
    public void addGuildEquipmentBatch(List<DbGuildEquipment> addGuildEquipmentList) {
        if (addGuildEquipmentList != null) {
            guildEquipmentDAO.insertEquipmentBatch(addGuildEquipmentList);
        }
    }

    /**
     * 删除公会道具
     *
     * @param deleteGuildEquipmentList
     */
    public void deleteEquipmentBatch(List<DbGuildEquipment> deleteGuildEquipmentList) {
        if (deleteGuildEquipmentList != null) {
            guildEquipmentDAO.deleteEquipmentBatch(deleteGuildEquipmentList);
        }
    }

    /**
     * 批量添加药剂
     *
     * @param addGuildPotionList
     */
    public void addGuildPotionBatch(List<DbGuildPotion> addGuildPotionList) {
        if (addGuildPotionList != null) {
            guildPotionDAO.insertPotionBatch(addGuildPotionList);
        }

    }

    /**
     * 批量删除公会药剂
     *
     * @param deleteGuildPotionList
     */
    public void deleteGuildPotionBatch(List<DbGuildPotion> deleteGuildPotionList) {
        if (deleteGuildPotionList != null) {
            guildPotionDAO.deletePotionBatch(deleteGuildPotionList);
        }
    }

    /**
     * 批量修改药剂
     *
     * @param modifyGuildPotionList
     */
    public void modifyGuildPotionBatch(List<DbGuildPotion> modifyGuildPotionList) {
        if (modifyGuildPotionList != null) {
            guildPotionDAO.updatePotionBatch(modifyGuildPotionList);
        }
    }

    /**
     * 通过公会id获取该公会所有装备
     *
     * @param guildId
     * @return
     */
    public List<DbGuildEquipment> listGuildEquipment(Integer guildId) {

        return guildEquipmentDAO.selectGuildEquipment(guildId);
    }

    /**
     * 通过公会id获取该工会所有药剂
     *
     * @param guildId
     * @return
     */
    public List<DbGuildPotion> listGuildPotion(Integer guildId) {
        return guildPotionDAO.selectGuildPotion(guildId);
    }

    /**
     * 批量修改公会信息
     * @param guildEntityCollection
     */
    public void modifyGuildEntityBatch(Collection<GuildEntity> guildEntityCollection) {
        if (guildEntityCollection != null) {
           guildEntityDAO.updateGuildBatch(guildEntityCollection);
        }
    }

    public void modifyGuildMemberBatch(Collection<GuildMemberEntity> guildMemberCollection) {
        if (guildMemberCollection!=null){
            guildMemberDAO.updateGuildMemberBatch(guildMemberCollection);
        }
    }
}
