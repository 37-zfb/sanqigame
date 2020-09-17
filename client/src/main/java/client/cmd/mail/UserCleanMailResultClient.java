package client.cmd.mail;

import client.cmd.ICmd;
import client.model.MailClient;
import client.model.Role;
import client.model.SceneData;
import client.model.client.MailEntityClient;
import client.CmdThread;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 */
public class UserCleanMailResultClient implements ICmd<GameMsg.UserCleanMailResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserCleanMailResult userCleanMailResult) {
        MyUtil.checkIsNull(ctx, userCleanMailResult);

        Role role = Role.getInstance();
        MailClient mail = role.getMail();
        Map<Long, MailEntityClient> mailMap = mail.getMailMap();
        List<GameMsg.MailInfo> mailInfoList = userCleanMailResult.getMailInfoList();
        for (GameMsg.MailInfo mailInfo : mailInfoList) {
            mailMap.put(mailInfo.getMailId(),new MailEntityClient(mailInfo.getMailId(),mailInfo.getSrcUserName(),mailInfo.getTitle()));
        }
        if (mailMap.size() > 0){
            mail.setHave(true);
        }else {
            mail.setHave(false);
        }

        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
    }
}
