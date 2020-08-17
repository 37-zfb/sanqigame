package client.cmd.mail;

import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class SendMailResultClient implements ICmd<GameMsg.SendMailResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.SendMailResult sendMailResult) {
        MyUtil.checkIsNull(ctx,sendMailResult);
        Role role = Role.getInstance();
        if (sendMailResult.getIsSuccess()){
            System.out.println("发送成功;");
        }else {
            System.out.println("发送失败;");
        }

    }
}
