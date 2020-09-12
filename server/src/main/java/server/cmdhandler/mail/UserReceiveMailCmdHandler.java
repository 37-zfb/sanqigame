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
import server.cmdhandler.duplicate.PropsUtil;
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

import java.util.Arrays;
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
                    // 此时未读0
                    receiveMail(user, mailEntity,newBuilder);
                }
            }
            newBuilder.setMoney(user.getMoney());
            GameMsg.UserReceiveMailResult userReceiveMailResult = newBuilder.setMoney(user.getMoney()).build();
            ctx.writeAndFlush(userReceiveMailResult);
            return;
        }


        // 领取对应的邮件; 把对应的道具、金币放进背包
        DbSendMailEntity mailEntity = mailMap.get(mailId);
        receiveMail(user, mailEntity,newBuilder);
        newBuilder.addMailId(mailEntity.getId());

        newBuilder.setMoney(user.getMoney());
        GameMsg.UserReceiveMailResult userReceiveMailResult = newBuilder.build();
        ctx.writeAndFlush(userReceiveMailResult);
    }


    /**
     * 领取邮件
     *
     * @param user
     * @param mailEntity
     */
    private void receiveMail(User user, DbSendMailEntity mailEntity, GameMsg.UserReceiveMailResult.Builder newBuilder) {
        try {

            String propsInfo = mailEntity.getPropsInfo();
            List<MailProps> mailProps = JSON.parseArray(propsInfo, MailProps.class);

            for (MailProps mailProp : mailProps) {
                PropsUtil.getPropsUtil().addProps(Arrays.asList(mailProp.getPropsId()), user, newBuilder, mailProp.getNumber());
            }

            mailEntity.setState(MailType.READ.getState());
            newBuilder.addMailId(mailEntity.getId());

            sendMailTimer.modifyMailList(mailEntity);

            user.setMoney(user.getMoney() + mailEntity.getMoney());
            log.info("用户 {} 从邮件中获取 {}金币;", user.getUserName(), mailEntity.getMoney());
            CurrUserStateEntity userState = PublicMethod.getInstance().createUserState(user);
            userStateTimer.modifyUserState(userState);

        } catch (CustomizeException e) {
            log.error("用户 {} 领取 {} 邮件失败;",user.getUserName(),mailEntity.getTitle());
            log.error(e.getMessage(), e);
        }

    }

}


