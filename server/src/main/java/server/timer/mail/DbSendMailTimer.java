package server.timer.mail;

import entity.db.DbSendMailEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.service.MailService;
import util.CustomizeThreadFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public final class DbSendMailTimer {
    public DbSendMailTimer() {
        persistenceDate();
    }

    @Autowired
    private MailService mailService;

    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("邮件持久化数据库;")
    );

    private List<DbSendMailEntity> addMailList = new CopyOnWriteArrayList<>();
    private List<DbSendMailEntity> modifyMailList = new CopyOnWriteArrayList<>();

    public void addMailList(DbSendMailEntity mailEntity) {
        addMailList.add(mailEntity);
    }

    public void modifyMailList(DbSendMailEntity mailEntity){
        modifyMailList.add(mailEntity);
    }

    public void persistenceDate() {
        scheduledThreadPool.scheduleWithFixedDelay(() -> {
            try {

                if (addMailList.size() != 0) {
                    mailService.addMailInfoBatch(addMailList);
                    addMailList.clear();
                    log.info("添加邮件信息;");
                }

                if (modifyMailList.size() != 0){
                    mailService.modifyMailState(modifyMailList);
                    modifyMailList.clear();
                    log.info("修改邮件信息;");
                }

            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }
        }, 30, 30, TimeUnit.SECONDS);

    }

}
