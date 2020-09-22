package server.cmdhandler.mail;

import com.alibaba.fastjson.JSON;
import entity.MailProps;
import entity.db.DbAllSendMailEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.service.AllSendMailService;
import server.util.IdWorker;
import util.MyUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author 张丰博
 * 发送全体邮件
 */
@Component
@Slf4j
public class SendAllMailCmdHandler implements ICmdHandler<GameMsg.SendAllMailCmd> {

    @Autowired
    private AllSendMailService mailService;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.SendAllMailCmd sendAllMailCmd) {

        MyUtil.checkIsNull(ctx, sendAllMailCmd);

        int money = sendAllMailCmd.getMoney();
        List<GameMsg.MailProps> propsList = sendAllMailCmd.getPropsList();
        String title = sendAllMailCmd.getTitle();

        List<MailProps> list = new ArrayList<>();
        propsList.forEach(p -> list.add(new MailProps(p.getPropsId(), p.getNumber())));

        String jsonString = JSON.toJSONString(list);

        DbAllSendMailEntity dbAllSendMailEntity = new DbAllSendMailEntity();
        dbAllSendMailEntity.setId(IdWorker.generateId());
        dbAllSendMailEntity.setDate(new Date());
        dbAllSendMailEntity.setMoney(money);
        dbAllSendMailEntity.setPropsInfo(jsonString);
        dbAllSendMailEntity.setSrcUserName("管理员");
        dbAllSendMailEntity.setTitle(title);

        mailService.addAllMail(dbAllSendMailEntity);
    }
}
