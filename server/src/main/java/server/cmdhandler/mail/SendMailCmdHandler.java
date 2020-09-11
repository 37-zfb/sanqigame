package server.cmdhandler.mail;

import com.alibaba.fastjson.JSON;
import constant.MailConst;
import entity.db.DbSendMailEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
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


    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.SendMailCmd sendMailCmd) {

        MyUtil.checkIsNull(ctx, sendMailCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        int targetUserId = sendMailCmd.getTargetUserId();
        if (targetUserId <= 0) {
            return;
        }

        List<GameMsg.MailProps> propsList = sendMailCmd.getPropsList();
        if (propsList.size() > MailConst.MAX_PROPS_NUMBER) {
            throw new CustomizeException(CustomizeErrorCode.MAIL_NUMBER_OVERFLOW);
        }

        List<MailProps> mailList = new ArrayList<>();
        for (GameMsg.MailProps mailProps : propsList) {
            mailList.add(new MailProps(mailProps.getPropsId(), mailProps.getNumber()));
        }

        int money = sendMailCmd.getMoney();
        String title = sendMailCmd.getTitle();

        MailUtil.getMailUtil().sendMail(targetUserId, money, title, mailList);


        GameMsg.SendMailResult sendMailResult = GameMsg.SendMailResult.newBuilder().build();
        ctx.writeAndFlush(sendMailResult);

    }
}
