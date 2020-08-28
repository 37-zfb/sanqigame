package server.timer.guild;

import entity.db.GuildEntity;
import entity.db.GuildMemberEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.GuildManager;
import server.model.PlayGuild;
import server.service.GuildService;
import util.CustomizeThreadFactory;

import javax.annotation.PostConstruct;
import java.util.*;
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


    public DbGuildTimer(){
        persistenceDate();
    }

    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("公会信息持久化数据库;")
    );

    private Map<Integer,GuildEntity> addGuildEntityMap = new HashMap<>();
    private Map<Integer,GuildMemberEntity> addGuildMemberEntityMap = new HashMap<>();


    private Map<Integer,GuildEntity> modifyGuildEntityMap = new HashMap<>();
    private Map<Integer,GuildMemberEntity> modifyGuildMemberEntityMap = new HashMap<>();

    private Map<Integer,GuildEntity> deleteGuildEntityMap = new HashMap<>();
    private Map<Integer,GuildMemberEntity> deleteGuildMemberEntityMap = new HashMap<>();

    /**
     * 添加公会信息
     *
     * @param guildEntity
     */
    public synchronized void addGuildEntity(GuildEntity guildEntity) {
        if (guildEntity == null) {
            return;
        }
        addGuildEntityMap.put(guildEntity.getPresidentId(),guildEntity);
    }

    /**
     * 添加公会成员信息
     *
     * @param guildMemberEntity
     */
    public synchronized void addGuildMemberEntity(GuildMemberEntity guildMemberEntity) {
        if (guildMemberEntity == null) {
            return;
        }
        addGuildMemberEntityMap.put(guildMemberEntity.getUserId(),guildMemberEntity);
    }

    /**
     * 修改公会信息
     *
     * @param guildEntity
     */
    public synchronized void modifyGuildEntity(GuildEntity guildEntity) {
        if (guildEntity == null) {
            return;
        }
        modifyGuildEntityMap.put(guildEntity.getPresidentId(),guildEntity);
    }

    /**
     * 修改公会成员信息
     *
     * @param guildMemberEntity
     */
    public synchronized void modifyGuildMemberEntity(GuildMemberEntity guildMemberEntity) {
        if (guildMemberEntity == null) {
            return;
        }
        modifyGuildMemberEntityMap.put(guildMemberEntity.getUserId(),guildMemberEntity);
    }


    /**
     * 删除公会信息
     *
     * @param guildEntity
     */
    public synchronized void deleteGuildEntity(GuildEntity guildEntity) {
        if (guildEntity == null) {
            return;
        }
        deleteGuildEntityMap.put(guildEntity.getPresidentId(),guildEntity);
    }

    /**
     * 删除公会成员信息
     *
     * @param guildMemberEntity
     */
    public synchronized void deleteGuildMemberEntity(GuildMemberEntity guildMemberEntity) {
        if (guildMemberEntity == null) {
            return;
        }
        deleteGuildMemberEntityMap.put(guildMemberEntity.getUserId(),guildMemberEntity);
    }
    public synchronized void deleteGuildMemberEntity(Map<Integer,GuildMemberEntity> guildMemberEntityMap) {
        if (guildMemberEntityMap == null) {
            return;
        }
        deleteGuildMemberEntityMap.putAll(guildMemberEntityMap);
    }


    private void persistenceDate() {
        scheduledThreadPool.scheduleWithFixedDelay(() -> {

            try {
                //添加 公会信息
                if (addGuildEntityMap.size() != 0) {
                    guildService.addGuildInfoBatch(addGuildEntityMap.values());
                    addGuildEntityMap.clear();
                    log.info("持久化公会信息;");
                }
                //添加公会成员
                if (addGuildMemberEntityMap.size() != 0) {
                    guildService.addGuildMemberBatch(addGuildMemberEntityMap.values());
                    addGuildMemberEntityMap.clear();
                    log.info("持久化公会成员信息;");
                }
                //修改公会信息
                if (modifyGuildEntityMap.size() != 0) {

                }
                //修改公会成员信息
                if (modifyGuildMemberEntityMap.size() != 0) {

                }
                //删除公会
                if (deleteGuildEntityMap.size() != 0) {
                    guildService.deleteGuildEntityBatch(deleteGuildEntityMap.values());
                    deleteGuildEntityMap.clear();
                    log.info("解散公会;");
                }
                //删除公会成员
                if (deleteGuildMemberEntityMap.size() != 0) {
                    guildService.deleteGuildMemberEntityBatch(deleteGuildMemberEntityMap.values());
                    deleteGuildMemberEntityMap.clear();
                    log.info("删除公会成员;");
                }
            }catch (Exception e){
                log.info(e.getMessage(),e);
            }


        }, 30, 30, TimeUnit.SECONDS);
    }


    /**
     *  初始化公会信息
     */
    @PostConstruct
    private void initGuildManager(){
        log.info("初始化公会消息;");
        List<GuildEntity> guildEntityList = guildService.listGuildInfo();
        List<GuildMemberEntity> guildMemberEntityList = guildService.listGuildMember();

        for (GuildEntity guildEntity : guildEntityList) {
            PlayGuild playGuild = new PlayGuild();
            playGuild.setGuildEntity(guildEntity);
            playGuild.setId(guildEntity.getId());

            for (GuildMemberEntity guildMemberEntity : guildMemberEntityList) {
                if (guildEntity.getId().equals(guildMemberEntity.getGuildId())){
                    playGuild.getGuildMemberMap().put(guildMemberEntity.getUserId(),guildMemberEntity);
                }
            }
            GuildManager.addGuild(playGuild);
        }

    }

}
