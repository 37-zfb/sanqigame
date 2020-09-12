package server.timer.guild;

import entity.db.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.GuildManager;
import server.model.PlayGuild;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import server.scene.GameData;
import server.service.GuildService;
import util.CustomizeThreadFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public class DbGuildTimer {

    @Autowired
    private GuildService guildService;


    public DbGuildTimer() {
        persistenceDate();
    }

    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("公会信息持久化数据库;")
    );

    private Map<Integer, GuildEntity> addGuildEntityMap = new ConcurrentHashMap<>();
    private Map<Integer, GuildMemberEntity> addGuildMemberEntityMap = new ConcurrentHashMap<>();
    private List<DbGuildEquipment> addGuildEquipmentList = new CopyOnWriteArrayList<>();
    private List<DbGuildPotion> addGuildPotionList = new CopyOnWriteArrayList<>();


    private Map<Integer, GuildEntity> modifyGuildEntityMap = new ConcurrentHashMap<>();
    private Map<Integer, GuildMemberEntity> modifyGuildMemberEntityMap = new ConcurrentHashMap<>();
    private List<DbGuildEquipment> modifyGuildEquipmentList = new CopyOnWriteArrayList<>();
    private List<DbGuildPotion> modifyGuildPotionList = new CopyOnWriteArrayList<>();


    private Map<Integer, GuildEntity> deleteGuildEntityMap = new ConcurrentHashMap<>();
    private Map<Integer, GuildMemberEntity> deleteGuildMemberEntityMap = new ConcurrentHashMap<>();
    private List<DbGuildEquipment> deleteGuildEquipmentList = new CopyOnWriteArrayList<>();
    private List<DbGuildPotion> deleteGuildPotionList = new CopyOnWriteArrayList<>();

    /**
     * 添加公会信息
     *
     * @param guildEntity
     */
    public void addGuildEntity(GuildEntity guildEntity) {
        if (guildEntity == null) {
            return;
        }
        addGuildEntityMap.put(guildEntity.getPresidentId(), guildEntity);
    }

    /**
     * 添加公会成员信息
     *
     * @param guildMemberEntity
     */
    public void addGuildMemberEntity(GuildMemberEntity guildMemberEntity) {
        if (guildMemberEntity == null) {
            return;
        }
        addGuildMemberEntityMap.put(guildMemberEntity.getUserId(), guildMemberEntity);
    }

    /**
     * 添加公会装备
     *
     * @param guildEquipment
     */
    public void addGuildEquipment(DbGuildEquipment guildEquipment) {
        if (guildEquipment == null) {
            return;
        }
        addGuildEquipmentList.add(guildEquipment);
    }

    /**
     * 添加公会药剂
     *
     * @param guildPotion
     */
    public void addGuildPotion(DbGuildPotion guildPotion) {
        if (guildPotion == null) {
            return;
        }
        addGuildPotionList.add(guildPotion);
    }


    /**
     * 修改公会信息
     *
     * @param guildEntity
     */
    public void modifyGuildEntity(GuildEntity guildEntity) {
        if (guildEntity == null) {
            return;
        }
        modifyGuildEntityMap.put(guildEntity.getId(), guildEntity);
    }

    /**
     * 修改公会成员信息
     *
     * @param guildMemberEntity
     */
    public void modifyGuildMemberEntity(GuildMemberEntity guildMemberEntity) {
        if (guildMemberEntity == null) {
            return;
        }
        modifyGuildMemberEntityMap.put(guildMemberEntity.getUserId(), guildMemberEntity);
    }

    /**
     * 修改公会装备
     *
     * @param guildEquipment
     */
    public void modifyGuildEquipment(DbGuildEquipment guildEquipment) {
        if (guildEquipment == null) {
            return;
        }
        modifyGuildEquipmentList.add(guildEquipment);
    }

    /**
     * 修改公会道具
     *
     * @param guildPotion
     */
    public void modifyGuildPotion(DbGuildPotion guildPotion) {
        if (guildPotion == null) {
            return;
        }
        modifyGuildPotionList.add(guildPotion);
    }


    /**
     * 删除公会信息
     *
     * @param guildEntity
     */
    public void deleteGuildEntity(GuildEntity guildEntity) {
        if (guildEntity == null) {
            return;
        }
        deleteGuildEntityMap.put(guildEntity.getPresidentId(), guildEntity);
    }

    /**
     * 删除公会成员信息
     *
     * @param guildMemberEntity
     */
    public void deleteGuildMemberEntity(GuildMemberEntity guildMemberEntity) {
        if (guildMemberEntity == null) {
            return;
        }
        deleteGuildMemberEntityMap.put(guildMemberEntity.getUserId(), guildMemberEntity);
    }

    public void deleteGuildMemberEntity(Map<Integer, GuildMemberEntity> guildMemberEntityMap) {
        if (guildMemberEntityMap == null) {
            return;
        }
        deleteGuildMemberEntityMap.putAll(guildMemberEntityMap);
    }

    /**
     * 删除公会装备信息
     *
     * @param guildEquipment
     */
    public void deleteGuildEquipment(DbGuildEquipment guildEquipment) {
        if (guildEquipment == null) {
            return;
        }
        deleteGuildEquipmentList.add(guildEquipment);
    }

    /**
     * 删除公会药剂信息
     *
     * @param guildPotion
     */
    public void deleteGuildPotion(DbGuildPotion guildPotion) {
        if (guildPotion == null) {
            return;
        }
        deleteGuildPotionList.add(guildPotion);
    }


    private void persistenceDate() {
        scheduledThreadPool.scheduleWithFixedDelay(() -> {

            try {
                //添加 公会信息
                if (addGuildEntityMap.size() != 0) {
                    guildService.addGuildInfoBatch(copyGuild(addGuildEntityMap));
                    log.info("持久化公会信息;");
                }
                //添加公会成员
                if (addGuildMemberEntityMap.size() != 0) {
                    guildService.addGuildMemberBatch(copyGuildMember(addGuildMemberEntityMap));
                    log.info("持久化公会成员信息;");
                }
                //添加公会装备
                if (addGuildEquipmentList.size() != 0) {
                    guildService.addGuildEquipmentBatch(copyGuildEqu(addGuildEquipmentList));
                    log.info("持久化公会仓库装备;");
                }
                //添加公会药剂
                if (addGuildPotionList.size() != 0) {
                    guildService.addGuildPotionBatch(copyGuildPot(addGuildPotionList));
                    log.info("持久化公会仓库药剂");
                }


                //修改公会信息
                if (modifyGuildEntityMap.size() != 0) {
                    guildService.modifyGuildEntityBatch(copyGuild(modifyGuildEntityMap));
                    log.info("修改公会信息;");
                }
                //修改公会成员信息
                if (modifyGuildMemberEntityMap.size() != 0) {
                    guildService.modifyGuildMemberBatch(copyGuildMember(modifyGuildMemberEntityMap));
                    log.info("修改公会成员信息;");
                }
                if (modifyGuildEquipmentList.size() != 0) {

                }
                // 修改药剂信息
                if (modifyGuildPotionList.size() != 0) {
                    guildService.modifyGuildPotionBatch(copyGuildPot(modifyGuildPotionList));
                    log.info("修改仓库药剂");
                }


                //删除公会
                if (deleteGuildEntityMap.size() != 0) {
                    guildService.deleteGuildEntityBatch(copyGuild(deleteGuildEntityMap));
                    log.info("解散公会;");
                }
                //删除公会成员
                if (deleteGuildMemberEntityMap.size() != 0) {
                    guildService.deleteGuildMemberEntityBatch(copyGuildMember(deleteGuildMemberEntityMap));
                    log.info("删除公会成员;");
                }
                // 删除
                if (deleteGuildEquipmentList.size() != 0) {
                    guildService.deleteEquipmentBatch(copyGuildEqu(deleteGuildEquipmentList));
                    log.info("删除公会装备");
                }
                if (deleteGuildPotionList.size() != 0) {
                    guildService.deleteGuildPotionBatch(copyGuildPot(deleteGuildPotionList));
                    log.info("删除公会药剂");
                }

            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }


        }, 30, 30, TimeUnit.SECONDS);
    }



    /**
     * 拷贝map中装备信息
     * @param guildEntityMap
     * @return
     */
    private List<GuildEntity> copyGuild(Map<Integer,GuildEntity> guildEntityMap) {
        List<GuildEntity> guildList = new ArrayList<>();
        if (guildEntityMap != null && guildEntityMap.size() != 0) {
            Iterator<Map.Entry<Integer, GuildEntity>> iterator = guildEntityMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, GuildEntity> next = iterator.next();
                guildList.add(next.getValue());
                iterator.remove();
            }
        }
        return guildList;
    }
    private List<GuildMemberEntity> copyGuildMember(Map<Integer,GuildMemberEntity> guildMemberMap) {
        List<GuildMemberEntity> guildMemberList = new ArrayList<>();
        if (guildMemberMap != null && guildMemberMap.size() != 0) {
            Iterator<Map.Entry<Integer, GuildMemberEntity>> iterator = guildMemberMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, GuildMemberEntity> next = iterator.next();
                guildMemberList.add(next.getValue());
                iterator.remove();
            }
        }
        return guildMemberList;
    }
    private List<DbGuildEquipment> copyGuildEqu(Collection<DbGuildEquipment> guildEquCollection) {
        List<DbGuildEquipment> guildEquList = new ArrayList<>();
        if (guildEquCollection != null && guildEquCollection.size() != 0) {
            guildEquList.addAll(guildEquCollection);
            guildEquCollection.removeAll(guildEquList);
        }
        return guildEquList;
    }
    private List<DbGuildPotion> copyGuildPot(Collection<DbGuildPotion> guildPotCollection) {
        List<DbGuildPotion> guildPotList = new ArrayList<>();
        if (guildPotCollection != null && guildPotCollection.size() != 0) {
            guildPotList.addAll(guildPotCollection);
            guildPotCollection.removeAll(guildPotList);
        }
        return guildPotList;
    }






    /**
     * 初始化公会信息
     */
    public void initGuildManager() {
        log.info("初始化公会消息;");
        //公会
        List<GuildEntity> guildEntityList = guildService.listGuildInfo();
        //公会成员
        List<GuildMemberEntity> guildMemberEntityList = guildService.listGuildMember();

        for (GuildEntity guildEntity : guildEntityList) {
            PlayGuild playGuild = new PlayGuild();
            playGuild.setGuildEntity(guildEntity);
            playGuild.setId(guildEntity.getId());
            playGuild.setWarehouseMoney(guildEntity.getMoney());

            for (GuildMemberEntity guildMemberEntity : guildMemberEntityList) {
                if (guildEntity.getId().equals(guildMemberEntity.getGuildId())) {
                    playGuild.getGuildMemberMap().put(guildMemberEntity.getUserId(), guildMemberEntity);
                }
            }

            Map<Integer, Props> warehouseProps = playGuild.getWAREHOUSE_PROPS();
            //公会仓库中的装备
            List<DbGuildEquipment> guildEquipmentList = guildService.listGuildEquipment(guildEntity.getId());
            for (DbGuildEquipment guildEquipment : guildEquipmentList) {
                Integer durability = guildEquipment.getDurability();
                Integer location = guildEquipment.getLocation();
                Integer propsId = guildEquipment.getPropsId();

                Props props = GameData.getInstance().getPropsMap().get(propsId);
                Equipment equipment = (Equipment) props.getPropsProperty();

                Props p = new Props();
                p.setName(props.getName());
                p.setId(props.getId());
                p.setPropsProperty(new Equipment(equipment.getId(), equipment.getPropsId(), durability, equipment.getDamage(), equipment.getEquipmentType()));

                warehouseProps.put(location, p);
            }

            //公会仓库中的药剂
            List<DbGuildPotion> guildPotionList = guildService.listGuildPotion(guildEntity.getId());
            for (DbGuildPotion guildPotion : guildPotionList) {
                Integer location = guildPotion.getLocation();
                Integer number = guildPotion.getNumber();
                Integer propsId = guildPotion.getPropsId();

                Props props = GameData.getInstance().getPropsMap().get(propsId);
                Potion potion = (Potion) props.getPropsProperty();

                Props p = new Props();
                p.setName(props.getName());
                p.setId(props.getId());
                p.setPropsProperty(new Potion(potion.getId(), potion.getPropsId(), potion.getCdTime(), potion.getInfo(), potion.getResumeFigure(), potion.getPercent(), number));

                warehouseProps.put(location, p);
            }

            GuildManager.addGuild(playGuild);
        }

    }

}
