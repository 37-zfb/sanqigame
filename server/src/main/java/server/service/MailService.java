package server.service;

import entity.db.DbSendMailEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import org.slf4j.impl.Log4jLoggerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.dao.ISendMailDAO;
import type.MailType;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author 张丰博
 */
@Service
public class MailService {

    @Autowired
    private ISendMailDAO sendMailDAO;

    /**
     * 添加邮件信息
     *
     * @param dbSendMailEntity
     */
    public void addMailInfo(DbSendMailEntity dbSendMailEntity) {
        if (dbSendMailEntity == null) {
            throw new NullPointerException();
        }
        sendMailDAO.insertMail(dbSendMailEntity);
    }

    /**
     * 查询该用户十天内未读的邮件
     *
     * @param userId 用户id
     * @return
     */
    public List<DbSendMailEntity> listMailWithinTenDay(int userId) {
        if (userId <= 0) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_EXISTS);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE)-10);
        Date date = calendar.getTime();

        return sendMailDAO.selectMailWithinTenDay(userId, date);

    }

    @Transactional(rollbackFor = Exception.class)
    public void modifyMailState(Collection<DbSendMailEntity> mailCollection) {
        if (mailCollection == null || mailCollection.size() == 0 ){
            return;
        }
//        for (DbSendMailEntity mailEntity : mailCollection) {
//            if (mailEntity.getState().equals(MailType.UNREAD.getState())){
//                // 如果还是未读,则不进行处理
//                continue;
//            }
//        }
        sendMailDAO.updateMailBatch(mailCollection);
    }
}
