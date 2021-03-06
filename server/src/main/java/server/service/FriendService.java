package server.service;

import entity.db.DbFriendEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.dao.IFriendDAO;
import util.CustomizeThreadFactory;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author 张丰博
 */
@Service
@Slf4j
public class FriendService {

    @Autowired
    private IFriendDAO friendDAO;

   /* private final ExecutorService EX = new ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new CustomizeThreadFactory("朋友持久化;"),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );*/

    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("朋友信息持久化数据库;")
    );

    private final Object M = new Object();

    private List<DbFriendEntity> addFriendEntityList = new CopyOnWriteArrayList<>();
    private List<DbFriendEntity> deleteFriendEntityList = new CopyOnWriteArrayList<>();



    /**
     * 添加,
     *
     * @param friendEntity
     */
    public void addFriend(DbFriendEntity friendEntity) {
        if (friendEntity != null) {
            addFriendEntityList.add(friendEntity);
        }

    }

    /**
     * 删除好友
     *
     * @param friendEntity
     */
    public void deleteFriend(DbFriendEntity friendEntity) {
        if (friendEntity != null) {
            deleteFriendEntityList.add(friendEntity);
        }
    }

    @PostConstruct
    private void db() {

        scheduledThreadPool.scheduleWithFixedDelay(() -> {

            try {

                if (addFriendEntityList.size() != 0) {
                    friendDAO.insertBatch(copy(addFriendEntityList));
                    log.info("添加好友;");
                }
                if (deleteFriendEntityList.size() != 0) {
                    friendDAO.deleteBatch(copy(deleteFriendEntityList));
                    log.info("删除好友;");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }


        }, 3000, 3000, TimeUnit.MILLISECONDS);
        log.info("初始化好友定时器;");
    }

    private List<DbFriendEntity> copy(List<DbFriendEntity> dbFriendEntityList) {
        List<DbFriendEntity> targetList = new ArrayList<>(dbFriendEntityList);
        dbFriendEntityList.removeAll(targetList);
        return targetList;
    }

    public List<DbFriendEntity> listFriend(Integer userId){
        if (userId == null){
            return null;
        }

        return friendDAO.selectByUserId(userId);
    }

}
