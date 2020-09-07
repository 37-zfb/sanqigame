package client.cmd.task;

import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserUpLvResultClient implements ICmd<GameMsg.UserUpLvResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserUpLvResult userUpLvResult) {

        MyUtil.checkIsNull(ctx, userUpLvResult);
        Role role = Role.getInstance();

        int lv = userUpLvResult.getLv();
        role.setLv(lv);



    }
}
