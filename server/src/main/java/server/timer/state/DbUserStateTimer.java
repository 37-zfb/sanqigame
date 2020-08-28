package server.timer.state;

import entity.db.CurrUserStateEntity;
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
    private final Map<Integer,CurrUserStateEntity> userStateEntityMap = new HashMap<>();

    /**
     * 需要改变公会状态的 用户id集合 改变用户状态至未加入公会
     */
    private final Set<Integer> guildStatePublicSet = new HashSet<>();

    /**
     * 添加
     * @param currUserStateEntity
     */
    public void modifyUserState(CurrUserStateEntity currUserStateEntity) {
        if (currUserStateEntity != null) {
            userStateEntityMap.put(currUserStateEntity.getUserId(),currUserStateEntity);
        }
    }

    /**
     * 添加用户id； 此用户id是为了改变此用户的公会状态
     *
     * @param userId
     */
    public void addModifyGuildStateSet(Integer userId) {
        if (userId != null) {
            guildStatePublicSet.add(userId);
        }
    }

    private void updateDate() {
        scheduledThreadPool.scheduleWithFixedDelay(() -> {

            try {
                // 更新公会状态为 空
                if (guildStatePublicSet.size() != 0){
                    userService.modifyUserGuildState(guildStatePublicSet, GuildMemberType.Public.getRoleId());
                    guildStatePublicSet.clear();
                    log.info("修改用户公会状态;");
                }

            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }


        }, 30, 30, TimeUnit.SECONDS);
    }


}
