package server.cmdhandler.mail;

import com.alibaba.fastjson.JSON;
import constant.MailConst;
import entity.db.DbSendMailEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import entity.MailProps;
import server.model.User;
import server.model.UserManager;
import server.timer.mail.DbSendMailTimer;
import type.MailType;
import util.MyUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author 张丰博
 * 发送邮件
 */
@Slf4j
@Component
public class SendMailCmdHandler implements ICmdHandler<GameMsg.SendMailCmd> {



    @Autowired
    private DbSendMailTimer sendMailTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.SendMailCmd sendMailCmd) {

        MyUtil.checkIsNull(ctx, sendMailCmd);
        User user = PublicMethod.getInstance().getUser(ctx);
        int targetUserId = sendMailCmd.getTargetUserId();

        List<GameMsg.MailProps> propsList = sendMailCmd.getPropsList();
        if (propsList.size() > MailConst.MAX_PROPS_NUMBER){
            return;
        }

        DbSendMailEntity dbSendMailEntity = new DbSendMailEntity();
        dbSendMailEntity.setTargetUserId(targetUserId);
        dbSendMailEntity.setSrcUserId(sendMailCmd.getSrcUserId());
        dbSendMailEntity.setMoney(sendMailCmd.getMoney());
        dbSendMailEntity.setState(MailType.UNREAD.getState());
        dbSendMailEntity.setDate(new Date());
        dbSendMailEntity.setTitle(sendMailCmd.getTitle());
        dbSendMailEntity.setSrcUserName("管理员");

        List<MailProps> list = new ArrayList<>();
        for (GameMsg.MailProps mailProps : propsList) {
            list.add(new MailProps(mailProps.getPropsId(),mailProps.getNumber()));
        }
        String propsInfo = JSON.toJSONString(list);
        dbSendMailEntity.setPropsInfo(propsInfo);

        GameMsg.SendMailResult.Builder newBuilder = GameMsg.SendMailResult.newBuilder();
        try {
            sendMailTimer.addMailList(dbSendMailEntity);

            User targetUser = UserManager.getUserById(targetUserId);

            if (targetUser != null) {
                log.info("{} 发送给 {} 邮件;" + user.getUserName(), targetUser.getUserName());
                // 加入缓存中
                targetUser.getMail().addMail(dbSendMailEntity);

                GameMsg.MailInfo.Builder mailInfoBuilder = GameMsg.MailInfo.newBuilder()
                        .setTitle(dbSendMailEntity.getTitle())
                        .setMailId(dbSendMailEntity.getId())
                        .setSrcUserName(dbSendMailEntity.getSrcUserName());

                //此时目标用户在线，推送一个消息给目标用户
                GameMsg.NoticeUserGetMailResult getMailResult = GameMsg.NoticeUserGetMailResult.newBuilder().setMailInfo(mailInfoBuilder)
                        .build();
                targetUser.getCtx().writeAndFlush(getMailResult);
            }

            newBuilder.setIsSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            newBuilder.setIsSuccess(false);
        } finally {
            GameMsg.SendMailResult sendMailResult = newBuilder.build();
            ctx.writeAndFlush(sendMailResult);
        }

    }
}
