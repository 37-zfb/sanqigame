package server.timer.state;

import entity.db.CurrUserStateEntity;
import entity.db.UserEquipmentEntity;
import entity.db.UserPotionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.service.UserService;
import type.GuildMemberType;
import util.CustomizeThreadFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public class DbUserStateTimer {

    @Autowired
    private UserService userService;

    public DbUserStateTimer() {
        updateDate();
    }

    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("用户当前状态持久化数据库;")
    );

    /**
     * 当前用户状态
     */
    private final Map<Integer, CurrUserStateEntity> userStateEntityMap = new ConcurrentHashMap<>();

    /**
     * 用户id  装备对象    用户装备
     */
    private final Map<Integer, UserEquipmentEntity> addUserEquipmentEntityMap = new ConcurrentHashMap<>();
    private final Map<Integer, UserEquipmentEntity> modifyUserEquipmentEntityMap = new ConcurrentHashMap<>();
    private final Map<Integer, UserEquipmentEntity> deleteUserEquipmentEntityMap = new ConcurrentHashMap<>();

    /**
     * 用户id  装备对象    用户道具
     */
    private final Map<Integer, UserPotionEntity> addUserPotionEntityMap = new ConcurrentHashMap<>();
    private final Map<Integer, UserPotionEntity> modifyUserPotionEntityMap = new ConcurrentHashMap<>();
    private final Map<Integer, UserPotionEntity> deleteUserPotionEntityMap = new ConcurrentHashMap<>();

    /**
     * 添加
     *
     * @param currUserStateEntity
     */
    public void modifyUserState(CurrUserStateEntity currUserStateEntity) {
        if (currUserStateEntity != null) {
            userStateEntityMap.put(currUserStateEntity.getUserId(), currUserStateEntity);
        }
    }

    /**
     * 装备
     *
     * @param equipmentEntity
     */
    public void addUserEquipment(UserEquipmentEntity equipmentEntity) {
        if (equipmentEntity != null) {
            addUserEquipmentEntityMap.put(equipmentEntity.getUserId(), equipmentEntity);
        }
    }

    public void modifyUserEquipment(UserEquipmentEntity equipmentEntity) {
        if (equipmentEntity != null) {
            modifyUserEquipmentEntityMap.put(equipmentEntity.getUserId(), equipmentEntity);
        }
    }

    public void deleteUserEquipment(UserEquipmentEntity equipmentEntity) {
        if (equipmentEntity != null) {
            deleteUserEquipmentEntityMap.put(equipmentEntity.getUserId(), equipmentEntity);
        }
    }


    /**
     * 药剂
     *
     * @param potionEntity
     */
    public void addUserPotion(UserPotionEntity potionEntity) {
        if (potionEntity != null) {
            addUserPotionEntityMap.put(potionEntity.getUserId(), potionEntity);
        }
    }

    public void modifyUserPotion(UserPotionEntity potionEntity) {
        if (potionEntity != null) {
            modifyUserPotionEntityMap.put(potionEntity.getUserId(), potionEntity);
        }
    }

    public void deleteUserPotion(UserPotionEntity potionEntity) {
        if (potionEntity != null) {
            deleteUserPotionEntityMap.put(potionEntity.getUserId(), potionEntity);
        }
    }


    private void updateDate() {
        scheduledThreadPool.scheduleWithFixedDelay(() -> {

            try {
                // 更新公会状态为 空
                if (userStateEntityMap.size() != 0) {
                    userService.modifyUserGuildState(userStateEntityMap.values());
                    userStateEntityMap.clear();
                    log.info("修改用户状态;");
                }

                if (addUserEquipmentEntityMap.size() != 0) {
                    userService.addEquipmentBatch(addUserEquipmentEntityMap.values());
                    addUserEquipmentEntityMap.clear();
                    log.info("添加装备;");
                }
                if (modifyUserEquipmentEntityMap.size() != 0) {
                    userService.modifyEquipmentBatch(modifyUserEquipmentEntityMap.values());
                    modifyUserEquipmentEntityMap.clear();
                    log.info("修改装备;");
                }
                if (deleteUserEquipmentEntityMap.size() != 0) {
                    userService.deleteEquipmentBatch(deleteUserEquipmentEntityMap.values());
                    deleteUserEquipmentEntityMap.clear();
                    log.info("删除装备;");
                }

                if (addUserPotionEntityMap.size() != 0) {
                    userService.addPotionBatch(addUserPotionEntityMap.values());
                    addUserPotionEntityMap.clear();
                    log.info("添加用户药剂;");
                }
                if (modifyUserPotionEntityMap.size() != 0) {
                    userService.modifyPotionBatch(modifyUserPotionEntityMap.values());
                    modifyUserPotionEntityMap.clear();
                    log.info("修改用户药剂信息;");
                }
                if (deleteUserPotionEntityMap.size() != 0){
                    userService.deletePotionBatch(deleteUserPotionEntityMap.values());
                    deleteUserPotionEntityMap.clear();
                    log.info("删除用户药剂;");
                }


            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }


        }, 30, 30, TimeUnit.SECONDS);
    }


}
