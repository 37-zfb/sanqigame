package server.timer.task;

import com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList;
import entity.db.DbAuctionItemEntity;
import entity.db.DbTaskEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import util.CustomizeThreadFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public class DbTaskTimer {

    public DbTaskTimer() {
        persistenceDate();
    }

    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("任务持久化数据库;")
    );


    private final List<Object> addObjectList = new CopyOnWriteArrayList<>();
    private final List<Object> deleteObjectList = new CopyOnWriteArrayList<>();
    private final List<Object> modifyObjectList = new CopyOnWriteArrayList<>();

    public void addObject(Object o) {
        if (o != null) {
            addObjectList.add(o);
        }
    }

    public void deleteObject(Object o) {
        if (o != null) {
            deleteObjectList.add(o);
        }
    }

    public void modifyObject(Object o) {
        if (o != null) {
            modifyObjectList.add(o);
        }
    }


    private void persistenceDate() {

        scheduledThreadPool.scheduleWithFixedDelay(() -> {
            try {



            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }
        }, 30, 30, TimeUnit.SECONDS);

    }

}
