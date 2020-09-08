package server.service;

import entity.db.UserEquipmentEntity;
import entity.db.UserPotionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.dao.IUserEquipmentDAO;
import server.dao.IUserPotionDAO;
import util.CustomizeThreadFactory;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author 张丰博
 *
 */
@Service
@Slf4j
public class PropsService {


    private final ExecutorService EX = new ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new CustomizeThreadFactory("道具持久化;"),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("道具持久化数据库;")
    );

    @Autowired
    private IUserEquipmentDAO userEquipmentDAO;
    @Autowired
    private IUserPotionDAO userPotionDAO;


    /**
     * 装备id  装备对象    用户装备
     */
    private final Map<Integer, UserEquipmentEntity> addUserEquipmentEntityMap = new ConcurrentHashMap<>();
    private final Map<Integer, UserEquipmentEntity> modifyUserEquipmentEntityMap = new ConcurrentHashMap<>();
    private final Map<Integer, UserEquipmentEntity> deleteUserEquipmentEntityMap = new ConcurrentHashMap<>();

    /**
     * 药剂id  装备对象    用户道具
     */
    private final Map<Integer, UserPotionEntity> addUserPotionEntityMap = new ConcurrentHashMap<>();
    private final Map<Integer, UserPotionEntity> modifyUserPotionEntityMap = new ConcurrentHashMap<>();
    private final Map<Integer, UserPotionEntity> deleteUserPotionEntityMap = new ConcurrentHashMap<>();

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


    @PostConstruct
    private void dbPropsData() {
        scheduledThreadPool.scheduleWithFixedDelay(() -> {

            try {

                if (addUserEquipmentEntityMap.size() != 0) {
                    userEquipmentDAO.insertEquipmentBatch(copyEqu(addUserEquipmentEntityMap));
                    log.info("添加装备;");
                }
                if (modifyUserEquipmentEntityMap.size() != 0) {
                    userEquipmentDAO.updateEquipmentBatch(copyEqu(modifyUserEquipmentEntityMap));
                    log.info("修改装备;");
                }
                if (deleteUserEquipmentEntityMap.size() != 0) {
                    userEquipmentDAO.deleteEquipmentBatch(copyEqu(deleteUserEquipmentEntityMap));
                    log.info("删除装备;");
                }

                if (addUserPotionEntityMap.size() != 0) {
                    userPotionDAO.insertPotionBatch(copyPotion(addUserPotionEntityMap));
                    log.info("添加药剂;");
                }
                if (modifyUserPotionEntityMap.size() != 0) {
                    userPotionDAO.updatePotionBatch(copyPotion(modifyUserPotionEntityMap));
                    log.info("修改药剂信息;");
                }
                if (deleteUserPotionEntityMap.size() != 0) {
                    userPotionDAO.deletePotionBatch(copyPotion(deleteUserPotionEntityMap));
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
    private List<UserEquipmentEntity> copyEqu(Map<Integer, UserEquipmentEntity> equipmentEntityMap) {
        List<UserEquipmentEntity> equipmentList = new ArrayList<>();
        if (equipmentEntityMap != null && equipmentEntityMap.size() != 0) {
            Iterator<Map.Entry<Integer, UserEquipmentEntity>> iterator = equipmentEntityMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, UserEquipmentEntity> next = iterator.next();
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
    private List<UserPotionEntity> copyPotion(Map<Integer, UserPotionEntity> potionEntityMap) {
        List<UserPotionEntity> potionList = new ArrayList<>();
        if (potionEntityMap != null && potionEntityMap.size() != 0) {
            Iterator<Map.Entry<Integer, UserPotionEntity>> iterator = potionEntityMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, UserPotionEntity> next = iterator.next();
                potionList.add(next.getValue());
                iterator.remove();
            }
        }
        return potionList;
    }

}
