package server.service;

import constant.EquipmentConst;
import constant.ProfessionConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.dao.IUserDAO;
import server.dao.IUserEquipmentDAO;
import server.dao.IUserPotionDAO;
import server.dao.IUserStateDAO;
import entity.db.CurrUserStateEntity;
import entity.db.UserEntity;
import entity.db.UserEquipmentEntity;
import entity.db.UserPotionEntity;
import server.exception.CustomizeErrorCode;
import server.exception.CustomizeException;
import model.profession.Profession;
import scene.GameData;

import java.util.List;

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
            user = null;
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_FOUND);
        }
        user = userDAO.selectUserByName(userName);
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
     * @param equipmentEntity 装备对象
     */
    public Integer addEquipment(UserEquipmentEntity equipmentEntity) {
        if (equipmentEntity == null) {
            log.error("equipmentEntity对象为空!");
            return null;
        }
        userEquipmentDAO.insertEquipment(equipmentEntity);
        return equipmentEntity.getId();
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
    public void addPotion(UserPotionEntity userPotionEntity) {
        if (userPotionEntity == null) {
            log.error("userPotionEntity对象为空!");
            return;
        }
        UserPotionEntity entity = userPotionDAO.selectPotionByUserIdAndPotionId(userPotionEntity.getUserId(), userPotionEntity.getPropsId());
        if (entity == null) {
            // 没有该药剂
            userPotionDAO.insertPotion(userPotionEntity);
        } else if (entity.getNumber() < 99) {
            // 已有药剂，且未达到上限
            entity.setNumber(entity.getNumber() + 1);
            userPotionDAO.updatePotionNumber(entity);
        } else if (entity.getNumber() >= 99) {
            // 已有药剂，且达到上限,此时不在添加

        }

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
     *  顽疾使用药剂
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
     *  改变穿戴的装备
     * @param userEquipmentId
     */
    public void modifyWearEquipment(Integer userEquipmentId,Integer state,Integer location) {
        if (userEquipmentId==null || state == null || location == null){
            log.info("userEquipmentId={}, state={}, location={}",userEquipmentId,state,location);
            return;
        }
        userEquipmentDAO.updateEquipmentState(userEquipmentId,state,location);
    }

    public List<UserEquipmentEntity> listEquipmentWeared(Integer userId, Integer state) {
        if (userId == null || state==null){
            log.info("userId={},state={}",userId,state);
            return null;
        }
        return userEquipmentDAO.selectEquipmentByUserIdAndState(userId,state);
    }

    public void modifyEquipmentDurability(Integer id, Integer durability) {
        if (id == null || durability == null){
            log.info("id = {},durability = {}",id,durability);
            return;
        }
        userEquipmentDAO.updateEquipmentDurability(id,durability);
    }
}
