package client.cmd.mail;

import client.thread.CmdThread;
import client.cmd.ICmd;
import client.model.MailClient;
import client.model.Role;
import client.model.SceneData;
import client.model.client.MailEntityClient;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import type.MailType;
import util.MyUtil;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 */
@Slf4j
public class UserReceiveMailResultClient implements ICmd<GameMsg.UserReceiveMailResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserReceiveMailResult userReceiveMailResult) {

        MyUtil.checkIsNull(ctx, userReceiveMailResult);

        List<Integer> mailIdList = userReceiveMailResult.getMailIdList();
        Role role = Role.getInstance();
        MailClient mail = role.getMail();
        Map<Integer, MailEntityClient> mailMap = mail.getMailMap();
        for (Integer mailId : mailIdList) {
            mailMap.get(mailId).setMailType(MailType.READ);
        }
//        if (mailMap.size() <= 0) {
//            mail.setHave(false);
//        }

        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
    }
}
