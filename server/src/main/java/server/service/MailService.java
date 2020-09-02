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
import java.util.*;

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
     * 查询该用户所有未读邮件，再把过期邮件持久化到数据库设置为过期
     * @param userId 用户id
     * @return
     */
    public List<DbSendMailEntity> listMailWithinTenDay(int userId) {
        if (userId <= 0) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_EXISTS);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 10);
        Date date = calendar.getTime();

        List<DbSendMailEntity> dbSendMailEntityList = sendMailDAO.selectMailUnread(userId);
        List<DbSendMailEntity> unreadMail = new ArrayList<>();
        List<DbSendMailEntity> overdueMail = new ArrayList<>();
        for (DbSendMailEntity dbSendMailEntity : dbSendMailEntityList) {
            if (dbSendMailEntity.getDate().getTime() < date.getTime()) {
                // 此时已过期
                dbSendMailEntity.setState(MailType.EXPIRED.getState());
                overdueMail.add(dbSendMailEntity);
            } else {
                // 此时未过期
                unreadMail.add(dbSendMailEntity);
            }
        }
        // 把已过期的持久化到数据库
        if (overdueMail.size() >0){
            sendMailDAO.updateMailBatch(overdueMail);
        }
        return unreadMail;
    }

    /**
     * 改变邮件状态
     * @param mailCollection
     */
    @Transactional(rollbackFor = Exception.class)
    public void modifyMailState(Collection<DbSendMailEntity> mailCollection) {
        if (mailCollection == null || mailCollection.size() == 0) {
            return;
        }
        sendMailDAO.updateMailBatch(mailCollection);
    }

    /**
     * 通过邮件用户id和邮件标题获取邮件信息
     * @param userId
     * @param title
     * @return
     */
    public DbSendMailEntity findMailInfoByUserIdAndTitle(int userId, String title) {
        if (userId <=0 || title == null){
            return null;
        }
        return sendMailDAO.selectMailByUserIdAndTitle(userId,title);
    }

    /**
     * 添加邮件信息
     * @param addMailList
     */
    public void addMailInfoBatch(List<DbSendMailEntity> addMailList) {
        if (addMailList == null || addMailList.size()==0){
            return;
        }
        sendMailDAO.insertMailBatch(addMailList);
    }
}
