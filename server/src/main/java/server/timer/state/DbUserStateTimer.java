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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 * 定时更新用户状态、道具
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
    private final Map<Long, UserEquipmentEntity> addUserEquipmentEntityMap = new ConcurrentHashMap<>();
    private final Map<Long, UserEquipmentEntity> modifyUserEquipmentEntityMap = new ConcurrentHashMap<>();
    private final Map<Long, UserEquipmentEntity> deleteUserEquipmentEntityMap = new ConcurrentHashMap<>();

    /**
     * 用户id  装备对象    用户道具
     */
    private final Map<Long, UserPotionEntity> addUserPotionEntityMap = new ConcurrentHashMap<>();
    private final Map<Long, UserPotionEntity> modifyUserPotionEntityMap = new ConcurrentHashMap<>();
    private final Map<Long, UserPotionEntity> deleteUserPotionEntityMap = new ConcurrentHashMap<>();

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
            addUserEquipmentEntityMap.put(equipmentEntity.getId(), equipmentEntity);
        }
    }

    public void modifyUserEquipment(UserEquipmentEntity equipmentEntity) {
        if (equipmentEntity != null) {
            modifyUserEquipmentEntityMap.put(equipmentEntity.getId(), equipmentEntity);
        }
    }

    public void deleteUserEquipment(UserEquipmentEntity equipmentEntity) {
        if (equipmentEntity != null) {
            deleteUserEquipmentEntityMap.put(equipmentEntity.getId(), equipmentEntity);
        }
    }


    /**
     * 药剂
     *
     * @param potionEntity
     */
    public void addUserPotion(UserPotionEntity potionEntity) {
        if (potionEntity != null) {
            addUserPotionEntityMap.put(potionEntity.getId(), potionEntity);
        }
    }

    public void modifyUserPotion(UserPotionEntity potionEntity) {
        if (potionEntity != null) {
            modifyUserPotionEntityMap.put(potionEntity.getId(), potionEntity);
        }
    }

    public void deleteUserPotion(UserPotionEntity potionEntity) {
        if (potionEntity != null) {
            deleteUserPotionEntityMap.put(potionEntity.getId(), potionEntity);
        }
    }


    private void updateDate() {
        scheduledThreadPool.scheduleWithFixedDelay(() -> {

            try {
                // 更新公会状态为 空
                if (userStateEntityMap.size() != 0) {
                    userService.modifyUserGuildState(copyUser(userStateEntityMap));
                    log.info("修改用户状态;");
                }

                if (addUserEquipmentEntityMap.size() != 0) {
                    userService.addEquipmentBatch(copyEqu(addUserEquipmentEntityMap));
                    log.info("添加装备;");
                }
                if (modifyUserEquipmentEntityMap.size() != 0) {
                    userService.modifyEquipmentBatch(copyEqu(modifyUserEquipmentEntityMap));
                    log.info("修改装备;");
                }
                if (deleteUserEquipmentEntityMap.size() != 0) {
                    userService.deleteEquipmentBatch(copyEqu(deleteUserEquipmentEntityMap));
                    log.info("删除装备;");
                }

                if (addUserPotionEntityMap.size() != 0) {
                    userService.addPotionBatch(copyPotion(addUserPotionEntityMap));
                    log.info("添加用户药剂;");
                }
                if (modifyUserPotionEntityMap.size() != 0) {
                    userService.modifyPotionBatch(copyPotion(modifyUserPotionEntityMap));
                    log.info("修改用户药剂信息;");
                }
                if (deleteUserPotionEntityMap.size() != 0){
                    userService.deletePotionBatch(copyPotion(deleteUserPotionEntityMap));
                    log.info("删除用户药剂;");
                }

            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }


        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * 拷贝map中装备信息
     * @param equipmentEntityMap
     * @return
     */
    private List<UserEquipmentEntity> copyEqu(Map<Long, UserEquipmentEntity> equipmentEntityMap) {
        List<UserEquipmentEntity> equipmentList = new ArrayList<>();
        if (equipmentEntityMap != null && equipmentEntityMap.size() != 0) {
            Iterator<Map.Entry<Long, UserEquipmentEntity>> iterator = equipmentEntityMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, UserEquipmentEntity> next = iterator.next();
                equipmentList.add(next.getValue());
                iterator.remove();
            }
        }
        return equipmentList;
    }

    /**
     * 拷贝map中药剂信息
     * @param potionEntityMap
     * @return
     */
    private List<UserPotionEntity> copyPotion(Map<Long, UserPotionEntity> potionEntityMap) {
        List<UserPotionEntity> potionList = new ArrayList<>();
        if (potionEntityMap != null && potionEntityMap.size() != 0) {
            Iterator<Map.Entry<Long, UserPotionEntity>> iterator = potionEntityMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, UserPotionEntity> next = iterator.next();
                potionList.add(next.getValue());
                iterator.remove();
            }
        }
        return potionList;
    }

    /**
     * 拷贝map中用户信息
     * @param userStateEntityMap
     * @return
     */
    private List<CurrUserStateEntity> copyUser(Map<Integer,CurrUserStateEntity > userStateEntityMap) {
        List<CurrUserStateEntity> userList = new ArrayList<>();
        if (userStateEntityMap != null && userStateEntityMap.size() != 0) {
            Iterator<Map.Entry<Integer, CurrUserStateEntity>> iterator = userStateEntityMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, CurrUserStateEntity> next = iterator.next();
                userList.add(next.getValue());
                iterator.remove();
            }
        }
        return userList;
    }


}
