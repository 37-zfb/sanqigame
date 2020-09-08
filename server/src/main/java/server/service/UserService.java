package server.service;

import constant.EquipmentConst;
import constant.ProfessionConst;
import entity.db.*;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import lombok.extern.slf4j.Slf4j;
import server.model.profession.Profession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.scene.GameData;
import server.dao.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author 张丰博
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IUserStateDAO userState;

    @Autowired
    private IUserEquipmentDAO userEquipmentDAO;

    @Autowired
    private IUserPotionDAO userPotionDAO;

    @Autowired
    private IUserGoodsLimitDAO userGoodsLimitDAO;

    /**
     * 通过userId获得当前用户信息
     *
     * @param userId 用户id
     * @return
     */
    public CurrUserStateEntity getCurrUserStateByUserId(Integer userId) {
        if (userId == null) {
            log.error("userId为空!");
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_FOUND);
        }
        CurrUserStateEntity currUserStateEntity = userState.selectUserStateByUserId(userId);
        return currUserStateEntity;
    }

    /**
     * 通过 用户名 查询，UserEntity
     *
     * @param userName 用户名
     * @return userEntity对象
     */
    public UserEntity getUserByName(String userName) {
        UserEntity user;
        if (userName == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_FOUND);
        }
        user = userDAO.selectUserByName(userName);

        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_FOUND);
        }

        return user;
    }


    /**
     * 添加用户信息，并返回id
     *
     * @param userEntity 用户对象
     * @return 用户id
     */
    @Transactional(rollbackFor = Exception.class)
    public int addUser(UserEntity userEntity) {
        if (userEntity == null) {
            log.error("userEntity对象为空!");
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_FOUND);
        }

        UserEntity user = userDAO.selectUserByName(userEntity.getUserName());
        if (user != null) {
            log.error("该名称已存在!");
            throw new CustomizeException(CustomizeErrorCode.USER_EXISTS);
        }
        // 添加用户
        Profession profession = GameData.getInstance().getProfessionMap().get(userEntity.getProfessionId());
        userDAO.insertInto(userEntity);
        CurrUserStateEntity currUserState = new CurrUserStateEntity();
        currUserState.setUserId(userEntity.getId());
        currUserState.setCurrHp(ProfessionConst.HP);
        currUserState.setCurrMp(ProfessionConst.MP);
        currUserState.setCurrSceneId(ProfessionConst.INIT_CURR_SCENE_ID);
        currUserState.setBaseDamage(profession.getBaseDamage());
        currUserState.setBaseDefense(profession.getBaseDefense());
        currUserState.setMoney(ProfessionConst.INIT_MONEY);

        // 添加用户状态
        userState.insertUserSate(currUserState);

        return userEntity.getId();
    }

    /**
     * 修改用户状态
     *
     * @param userStateEntity 用户状态对象
     */
    public void modifyUserState(CurrUserStateEntity userStateEntity) {
        if (userStateEntity == null) {
            log.error("userStateEntity对象为空!");
            return;
        }
        userState.updateUserState(userStateEntity);
    }


    /**
     * 添加装备
     * 返回userEquId
     *
     * @param equipmentEntity 装备对象
     */
    public Long addEquipment(UserEquipmentEntity equipmentEntity) {
        if (equipmentEntity == null) {
            log.error("equipmentEntity对象为空!");
            return null;
        }
        userEquipmentDAO.insertEquipment(equipmentEntity);
        return equipmentEntity.getId();
    }

    public void addEquipmentBatch(Collection<UserEquipmentEntity> equipmentCollection) {
        if (equipmentCollection == null) {
            log.error("equipmentCollection对象为空!");
            return;
        }
        userEquipmentDAO.insertEquipmentBatch(equipmentCollection);
    }


    /**
     * @param userId 用户id
     * @return 返回装备集合
     */
    public List<UserEquipmentEntity> listEquipment(Integer userId) {
        if (userId == null) {
            log.info("userId为空;");
            return null;
        }
        return userEquipmentDAO.selectEquipmentByUserIdAndState(userId, EquipmentConst.NO_WEAR);
    }

    /**
     * @param userPotionEntity
     */
    public Integer addPotion(UserPotionEntity userPotionEntity) {
        if (userPotionEntity == null) {
            log.error("userPotionEntity对象为空!");
            return null;
        }
        UserPotionEntity entity = userPotionDAO.selectPotionByUserIdAndPotionId(userPotionEntity.getUserId(), userPotionEntity.getPropsId());
        if (entity == null) {
            // 没有该药剂
            userPotionDAO.insertPotion(userPotionEntity);
            log.info("新添加道具的id {}", userPotionEntity.getId());
            return null;
        } else if (userPotionEntity.getNumber() < 99) {
            // 已有药剂，且未达到上限
            entity.setNumber(userPotionEntity.getNumber());
            userPotionDAO.updatePotionNumber(entity);
            return null;
        } else if (entity.getNumber() >= 99) {
            // 已有药剂，且达到上限,此时不在添加
            throw new CustomizeException(CustomizeErrorCode.PROPS_REACH_LIMIT);
        }

        return null;
    }

    /**
     * @param userId 用户id
     * @return 返回装备集合
     */
    public List<UserPotionEntity> listPotion(Integer userId) {
        if (userId == null) {
            log.info("userId为空;");
            return null;
        }
        return userPotionDAO.selectPotionByUserId(userId);
    }

    /**
     * 顽疾使用药剂
     *
     * @param entity
     * @return
     */
    public boolean usePotion(UserPotionEntity entity) {
        if (entity == null) {
            return false;
        }
        userPotionDAO.updatePotionNumber(entity);
        return true;
    }


    /**
     * 改变穿戴的装备
     *
     * @param userEquipmentId
     */
    public void modifyWearEquipment(Long userEquipmentId, Integer state, Integer location) {
        if (userEquipmentId == null || state == null || location == null) {
            log.info("userEquipmentId={}, state={}, location={}", userEquipmentId, state, location);
            return;
        }
        userEquipmentDAO.updateEquipmentState(userEquipmentId, state, location);
    }

    /**
     * 查询已穿戴的装备
     *
     * @param userId
     * @param state
     * @return
     */
    public List<UserEquipmentEntity> listEquipmentWeared(Integer userId, Integer state) {
        if (userId == null || state == null) {
            log.info("userId={},state={}", userId, state);
            return null;
        }
        return userEquipmentDAO.selectEquipmentByUserIdAndState(userId, state);
    }

    /**
     * 修改装备的耐久度
     *
     * @param id
     * @param durability
     */
    public void modifyEquipmentDurability(Integer id, Integer durability) {
        if (id == null || durability == null) {
            log.info("id = {},durability = {}", id, durability);
            return;
        }
        userEquipmentDAO.updateEquipmentDurability(id, durability);
    }

    /**
     * 修改 钱
     *
     * @param userId
     * @param money
     */
    public void modifyMoney(Integer userId, Integer money) {
        if (userId == null || money == null) {
            return;
        }
        userState.updateUserMoney(userId, money);
    }

    /**
     * 查询用户当天限制商品购买数
     *
     * @param userId
     * @param date
     * @return
     */
    public List<UserBuyGoodsLimitEntity> listUserBuyGoodsLimitEntity(Integer userId, String date) {
        if (userId == null || date == null) {
            return null;
        }
        return userGoodsLimitDAO.selectEntitiesByUserIdAndDate(userId, date);
    }


    /**
     * 添加购买数量
     *
     * @param goodsLimitEntity
     */
    public void addLimitNumber(UserBuyGoodsLimitEntity goodsLimitEntity) {
        if (goodsLimitEntity == null) {
            return;
        }
        userGoodsLimitDAO.insertEntity(goodsLimitEntity);
    }

    /**
     * 修改购买数量
     *
     * @param userBuyGoodsLimitEntity
     */
    public void modifyLimitNumber(UserBuyGoodsLimitEntity userBuyGoodsLimitEntity) {
        if (userBuyGoodsLimitEntity == null) {
            return;
        }
        userGoodsLimitDAO.updateLimitNumber(userBuyGoodsLimitEntity);
    }

    /**
     * 查询所有用户
     *
     * @return
     */
    public List<UserEntity> listUser() {
        return userDAO.selectAllUser();
    }

    /**
     * 更新用户 公会 状态
     *
     * @param stateSCollection
     */
    public void modifyUserGuildState(Collection<CurrUserStateEntity> stateSCollection) {
        userState.updateUserStateBatch(stateSCollection);
    }

    /**
     * 批量删除装备
     *
     * @param equipmentCollection
     */
    public void deleteEquipmentBatch(Collection<UserEquipmentEntity> equipmentCollection) {
        if (equipmentCollection != null) {
            userEquipmentDAO.deleteEquipmentBatch(equipmentCollection);
        }
    }

    /**
     * 批量修改装备
     *
     * @param userEquipmentCollection
     */
    public void modifyEquipmentBatch(Collection<UserEquipmentEntity> userEquipmentCollection) {
        if (userEquipmentCollection != null) {
            userEquipmentDAO.modifyEquipmentBatch(userEquipmentCollection);
        }
    }

    /**
     * 批量添加药剂
     *
     * @param potionEntityCollection
     */
    public void addPotionBatch(Collection<UserPotionEntity> potionEntityCollection) {
        if (potionEntityCollection != null) {
            userPotionDAO.insertPotionBatch(potionEntityCollection);
        }
    }

    /**
     * 批量修改药剂
     *
     * @param userPotionCollection
     */
    public void modifyPotionBatch(Collection<UserPotionEntity> userPotionCollection) {
        if (userPotionCollection != null) {
            int i = userPotionDAO.updatePotionBatch(userPotionCollection);
//            System.out.println("更新结果 "+i);
        }
    }

    /**
     * 批量删除药剂
     *
     * @param userPotionCollection
     */
    public void deletePotionBatch(Collection<UserPotionEntity> userPotionCollection) {
        if (userPotionCollection != null) {
            userPotionDAO.deletePotionBatch(userPotionCollection);
        }
    }
}
