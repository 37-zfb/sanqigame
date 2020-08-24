package server.cmdhandler.mail;

import constant.MailConst;
import entity.db.DbSendMailEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 * 清理邮件处理类；
 * 若邮件满了，清理邮件时把未读取的邮件推送回去
 */
@Component
@Slf4j
public class UserCleanMailCmdHandler implements ICmdHandler<GameMsg.UserCleanMailCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserCleanMailCmd userCleanMailCmd) {
        MyUtil.checkIsNull(ctx, userCleanMailCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        GameMsg.UserCleanMailResult.Builder newBuilder = GameMsg.UserCleanMailResult.newBuilder();
        Map<Integer, DbSendMailEntity> mailEntityMap = user.getMail().getMailEntityMap();
        for (DbSendMailEntity dbSendMailEntity : mailEntityMap.values()) {
            if (newBuilder.getMailInfoCount() == MailConst.MAX_SHOW_NUMBER) {
                break;
            }

            GameMsg.MailInfo.Builder mailInfo = GameMsg.MailInfo.newBuilder().setMailId(dbSendMailEntity.getId())
                    .setTitle(dbSendMailEntity.getTitle())
                    .setSrcUserName(dbSendMailEntity.getSrcUserName());
            newBuilder.addMailInfo(mailInfo);
        }


        GameMsg.UserCleanMailResult build = newBuilder.build();
        ctx.writeAndFlush(build);

    }
}
