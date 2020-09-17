package client.cmd.duplicate;

import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserAutoSubHpResultClient implements ICmd<GameMsg.UserAutoSubHpResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserAutoSubHpResult userAutoSubHpResult) {

        MyUtil.checkIsNull(ctx, userAutoSubHpResult);
        Role role = Role.getInstance();

        role.setCurrHp(role.getCurrHp() - userAutoSubHpResult.getSubHp());

        System.out.println("自动掉血: "+userAutoSubHpResult.getSubHp());
    }
}
