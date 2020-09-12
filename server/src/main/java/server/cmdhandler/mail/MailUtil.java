package server.cmdhandler.mail;

import com.alibaba.fastjson.JSON;
import entity.MailProps;
import entity.db.DbSendMailEntity;
import msg.GameMsg;
import server.GameServer;
import server.model.User;
import server.model.UserManager;
import server.timer.mail.DbSendMailTimer;
import server.util.IdWorker;
import type.MailType;

import java.util.Date;
import java.util.List;

/**
 * @author 张丰博
 */
public class MailUtil {

    private final static MailUtil MAIL_UTIL = new MailUtil();

    private static final DbSendMailTimer sendMailTimer = GameServer.APPLICATION_CONTEXT.getBean(DbSendMailTimer.class);

    private MailUtil(){}

    public static MailUtil getMailUtil(){
        return MAIL_UTIL;
    }

    /**
     *
     * @param targetUserId
     * @param money
     */
    public  void sendMail(Integer targetUserId, Integer money, String title, List<MailProps> propsList){
        //发送邮件
        DbSendMailEntity dbSendMailEntity = new DbSendMailEntity();
        dbSendMailEntity.setId(IdWorker.generateId());
        dbSendMailEntity.setTargetUserId(targetUserId);
        dbSendMailEntity.setSrcUserId(0);
        dbSendMailEntity.setMoney(money);
        dbSendMailEntity.setState(MailType.UNREAD.getState());
        dbSendMailEntity.setDate(new Date());
        dbSendMailEntity.setTitle(title);
        dbSendMailEntity.setSrcUserName("管理员");

//        List<MailProps> list = new ArrayList<>();
        String propsInfo = JSON.toJSONString(propsList);

        dbSendMailEntity.setPropsInfo(propsInfo);
        sendMailTimer.addMailList(dbSendMailEntity);

        User user = UserManager.getUserById(targetUserId);
        if (user!=null){
            send(dbSendMailEntity, user);
        }

    }


    /**
     * 发送邮件
     * @param dbSendMailEntity
     * @param user
     */
    private static void send(DbSendMailEntity dbSendMailEntity,User user){
        //用户添加邮件
        user.getMail().addMail(dbSendMailEntity);

        GameMsg.MailInfo.Builder mailInfoBuilder = GameMsg.MailInfo.newBuilder()
                .setTitle(dbSendMailEntity.getTitle())
                .setMailId(dbSendMailEntity.getId())
                .setSrcUserName(dbSendMailEntity.getSrcUserName());

        GameMsg.NoticeUserGetMailResult getMailResult = GameMsg.NoticeUserGetMailResult
                .newBuilder()
                .setMailInfo(mailInfoBuilder)
                .build();
        user.getCtx().writeAndFlush(getMailResult);
    }

}
