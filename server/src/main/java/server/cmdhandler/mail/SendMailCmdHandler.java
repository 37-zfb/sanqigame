package server.cmdhandler.mail;

import entity.db.DbSendMailEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.UserManager;
import server.service.MailService;
import type.MailType;
import util.MyUtil;

import java.util.Date;


/**
 * @author 张丰博
 */
@Slf4j
@Component
public class SendMailCmdHandler implements ICmdHandler<GameMsg.SendMailCmd> {

    @Autowired
    private MailService mailService;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.SendMailCmd sendMailCmd) {

        MyUtil.checkIsNull(ctx, sendMailCmd);
        User user = PublicMethod.getInstance().getUser(ctx);
        int targetUserId = sendMailCmd.getTargetUserId();

        DbSendMailEntity dbSendMailEntity = new DbSendMailEntity();
        dbSendMailEntity.setTargetUserId(targetUserId);
        dbSendMailEntity.setPropsId(sendMailCmd.getPropsId());
        dbSendMailEntity.setPropsNumber(sendMailCmd.getNumber());
        dbSendMailEntity.setSrcUserId(sendMailCmd.getSrcUserId());
        dbSendMailEntity.setMoney(sendMailCmd.getMoney());
        dbSendMailEntity.setState(MailType.UNREAD.getState());
        dbSendMailEntity.setDate(new Date());
        dbSendMailEntity.setTitle(sendMailCmd.getTitle());
        dbSendMailEntity.setSrcUserName("管理员");

        GameMsg.SendMailResult.Builder newBuilder = GameMsg.SendMailResult.newBuilder();
        try {
            mailService.addMailInfo(dbSendMailEntity);

            User targetUser = UserManager.getUserById(targetUserId);

            log.info("{} 发送给 {} 邮件;" + user.getUserName(), targetUser.getUserName());
            if (targetUser != null) {
                // 加入缓存中
                targetUser.getMail().getMailEntityMap().put(dbSendMailEntity.getId(),dbSendMailEntity);

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
