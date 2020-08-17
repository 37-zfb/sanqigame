package client.cmd.mail;

import client.cmd.ICmd;
import client.model.Role;
import client.model.client.MailEntityClient;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class NoticeUserGetMailResultClient implements ICmd<GameMsg.NoticeUserGetMailResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.NoticeUserGetMailResult noticeUserGetMailResult) {
        MyUtil.checkIsNull(ctx,noticeUserGetMailResult);

        Role role = Role.getInstance();
        role.getMail().setHave(true);

        GameMsg.MailInfo mailInfo = noticeUserGetMailResult.getMailInfo();
        role.getMail().getMailMap().put(mailInfo.getMailId(),new MailEntityClient(mailInfo.getMailId(),mailInfo.getSrcUserName(),mailInfo.getTitle()));

        System.out.println("收到邮件!");

    }
}
