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
import type.MailType;
import util.MyUtil;

import java.util.Iterator;
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
        Map<Long, DbSendMailEntity> mailEntityMap = user.getMail().getMailEntityMap();


        Iterator<Map.Entry<Long, DbSendMailEntity>> iterator = mailEntityMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Long, DbSendMailEntity> next = iterator.next();
            if (next.getValue().getState().equals(MailType.READ.getState())){
                iterator.remove();
                log.info("用户 {} 已读 {} 邮件;", user.getUserName(),next.getValue().getTitle());
                continue;
            }

            if (newBuilder.getMailInfoCount() == MailConst.MAX_SHOW_NUMBER) {
                break;
            }

            GameMsg.MailInfo.Builder mailInfo = GameMsg.MailInfo.newBuilder().setMailId(next.getValue().getId())
                    .setTitle(next.getValue().getTitle())
                    .setSrcUserName(next.getValue().getSrcUserName());
            log.info("用户 {} 未读 {} 邮件;",user.getUserName(),next.getValue().getTitle());
            newBuilder.addMailInfo(mailInfo);

        }


        GameMsg.UserCleanMailResult build = newBuilder.build();
        ctx.writeAndFlush(build);

    }
}
