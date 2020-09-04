package server.service;

import entity.db.DbTaskEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.dao.ITaskDAO;
import util.CustomizeThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 */
@Service
@Slf4j
public class TaskService {

    private final ExecutorService EX = new ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new CustomizeThreadFactory("任务持久化;"),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Autowired
    private ITaskDAO taskDAO;

    /**
     * 修改玩家任务状态
     *
     * @param dbTaskEntity
     */
    public void modifyTaskState(DbTaskEntity dbTaskEntity) {
        if (dbTaskEntity != null) {
            EX.submit(() -> {
                try {

                    taskDAO.update(dbTaskEntity);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

            });
        }
    }

    /**
     * 通过userId获取当前任务
     *
     * @param userId
     */
    public DbTaskEntity getCurrTaskById(Integer userId) {
        DbTaskEntity dbTaskEntity = null;
        if (userId != null) {
            dbTaskEntity = taskDAO.selectByUserId(userId);
        }

        return dbTaskEntity;
    }

}
