package server.timer.mail;

import entity.db.DbSendMailEntity;
import entity.db.UserEquipmentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.service.MailService;
import util.CustomizeThreadFactory;

import java.util.ArrayList;
import java.util.Iterator;
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
        if (mailEntity == null){
            return;
        }
        addMailList.add(mailEntity);
    }

    public void modifyMailList(DbSendMailEntity mailEntity){
        if (mailEntity == null){
            return;
        }
        modifyMailList.add(mailEntity);
    }

    public void persistenceDate() {
        scheduledThreadPool.scheduleWithFixedDelay(() -> {
            try {

                if (addMailList.size() != 0) {
                    mailService.addMailInfoBatch(copyMailList(addMailList));
                    log.info("添加邮件信息;");
                }

                if (modifyMailList.size() != 0){
                    mailService.modifyMailState(copyMailList(modifyMailList));
                    log.info("修改邮件信息;");
                }

            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * 拷贝map中装备信息
     * @param srcMailList
     * @return
     */
    private List<DbSendMailEntity> copyMailList(List<DbSendMailEntity> srcMailList) {
        List<DbSendMailEntity> mailList = new ArrayList<>();
        if (srcMailList == null || srcMailList.size() == 0){
            return mailList;
        }
        Iterator<DbSendMailEntity> iterator = srcMailList.iterator();
        while (iterator.hasNext()){
            DbSendMailEntity next = iterator.next();
            mailList.add(next);
        }
        srcMailList.removeAll(mailList);
        return mailList;
    }

}
