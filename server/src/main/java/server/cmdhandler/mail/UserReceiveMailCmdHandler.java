package server.cmdhandler.mail;

import com.alibaba.fastjson.JSON;
import entity.db.CurrUserStateEntity;
import entity.db.DbSendMailEntity;
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
import server.model.props.AbstractPropsProperty;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import server.scene.GameData;
import server.timer.mail.DbSendMailTimer;
import server.timer.state.DbUserStateTimer;
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

    @Autowired
    private DbUserStateTimer userStateTimer;
    @Autowired
    private DbSendMailTimer sendMailTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserReceiveMailCmd userReceiveMailCmd) {
        MyUtil.checkIsNull(ctx, userReceiveMailCmd);
        PublicMethod publicMethod = PublicMethod.getInstance();
        User user = publicMethod.getUser(ctx);

        long mailId = userReceiveMailCmd.getMailId();
        Map<Long, DbSendMailEntity> mailMap = user.getMail().getMailEntityMap();

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

        sortOutBackpack(user,newBuilder);
    }


    private void sortOutBackpack(User user, GameMsg.UserReceiveMailResult.Builder newBuilder){
        //  背包中的道具(装备、药剂等)  , 客户端更新背包中的数据
        Map<Integer, Props> backpack = user.getBackpack();
        for (Map.Entry<Integer, Props> propsEntry : backpack.entrySet()) {
            GameMsg.Props.Builder propsResult = GameMsg.Props.newBuilder()
                    .setLocation(propsEntry.getKey())
                    .setPropsId(propsEntry.getValue().getId());

            AbstractPropsProperty propsProperty = propsEntry.getValue().getPropsProperty();
            if (propsProperty.getType() == PropsType.Equipment) {
                Equipment equipment = (Equipment) propsProperty;
                // equipment.getId() 是数据库中的user_equipment中的id
                propsResult.setDurability(equipment.getDurability()).setUserPropsId(equipment.getId());
            } else if (propsProperty.getType() == PropsType.Potion) {
                Potion potion = (Potion) propsProperty;
                //potion.getId() 是数据库中的 user_potion中的id
                propsResult.setPropsNumber(potion.getNumber())
                        .setUserPropsId(potion.getId());
            }
            newBuilder.addProps(propsResult);
        }
        newBuilder.setMoney(user.getMoney());
        GameMsg.UserReceiveMailResult userReceiveMailResult = newBuilder.build();
        user.getCtx().writeAndFlush(userReceiveMailResult);
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
                log.info("用户 {} 从邮件中获取 {};", user.getUserName(),props.getName());
            } else if (props.getPropsProperty().getType() == PropsType.Potion) {
                // 添加药剂
                publicMethod.addPotion(props, user, mailProp.getNumber());
                log.info("用户 {} 从邮件中获取 {} {}个;", user.getUserName(),props.getName(),mailProp.getNumber());
            }
        }
        mailEntity.setState(MailType.READ.getState());
        sendMailTimer.modifyMailList(mailEntity);

        user.setMoney(user.getMoney() + mailEntity.getMoney());
        log.info("用户 {} 从邮件中获取 {}金币;", user.getUserName(),mailEntity.getMoney());
        CurrUserStateEntity userState = PublicMethod.getInstance().createUserState(user);
        userStateTimer.modifyUserState(userState);
    }


}


