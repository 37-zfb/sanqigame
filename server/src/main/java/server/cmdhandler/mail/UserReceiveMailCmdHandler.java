package server.cmdhandler.mail;

import com.alibaba.fastjson.JSON;
import entity.db.DbSendMailEntity;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import server.model.MailProps;
import server.model.props.Props;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.scene.GameData;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import type.MailType;
import type.PropsType;
import util.MyUtil;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 * 领取邮件
 */
@Component
@Slf4j
public class UserReceiveMailCmdHandler implements ICmdHandler<GameMsg.UserReceiveMailCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserReceiveMailCmd userReceiveMailCmd) {
        MyUtil.checkIsNull(ctx, userReceiveMailCmd);
        PublicMethod publicMethod = PublicMethod.getInstance();
        User user = publicMethod.getUser(ctx);

        int mailId = userReceiveMailCmd.getMailId();
        Map<Integer, DbSendMailEntity> mailMap = user.getMail().getMailEntityMap();

        GameMsg.UserReceiveMailResult.Builder newBuilder = GameMsg.UserReceiveMailResult.newBuilder();

        if (mailId == MailType.RECEIVE_ALL.getState()) {
            // 全部领取; 把邮件中的道具、金币放进背包
            for (DbSendMailEntity mailEntity : mailMap.values()) {
                if (mailEntity.getState().equals(MailType.UNREAD.getState())) {
                    // 此时未读
                    try {
                        receiveMail(user, mailEntity);
                        newBuilder.addMailId(mailEntity.getId());
                    } catch (CustomizeException e) {
                        log.info(e.getMessage(), e);
                    }
                }
            }
        } else {
            // 领取对应的邮件; 把对应的道具、金币放进背包
            DbSendMailEntity mailEntity = mailMap.get(mailId);
            try {
                receiveMail(user, mailEntity);
                newBuilder.addMailId(mailEntity.getId());
            } catch (CustomizeException e) {
                log.info(e.getMessage(), e);
            }
        }
        GameMsg.UserReceiveMailResult userReceiveMailResult = newBuilder.build();
        ctx.writeAndFlush(userReceiveMailResult);
    }

    /**
     *  领取邮件
     * @param user
     * @param mailEntity
     */
    private void receiveMail(User user, DbSendMailEntity mailEntity) {
        PublicMethod publicMethod = PublicMethod.getInstance();
        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();


        String propsInfo = mailEntity.getPropsInfo();
        List<MailProps> mailProps = JSON.parseArray(propsInfo, MailProps.class);

        for (MailProps mailProp : mailProps) {
            Props props = propsMap.get(mailProp.getPropsId());
            if (props.getPropsProperty().getType() == PropsType.Equipment) {
                // 添加装备
                publicMethod.addEquipment(user, props);
            } else if (props.getPropsProperty().getType() == PropsType.Potion) {
                // 添加药剂
                publicMethod.addPotion(props, user, mailProp.getNumber());
            }
        }

        mailEntity.setState(MailType.READ.getState());
        user.setMoney(user.getMoney() + mailEntity.getMoney());
    }


}


