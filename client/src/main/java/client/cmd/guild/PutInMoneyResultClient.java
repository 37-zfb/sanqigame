package client.cmd.guild;

import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class PutInMoneyResultClient implements ICmd<GameMsg.PutInMoneyResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.PutInMoneyResult putInMoneyResult) {

        MyUtil.checkIsNull(ctx, putInMoneyResult);

        int money = putInMoneyResult.getMoney();
        Role role = Role.getInstance();
        role.setMoney(role.getMoney() - money);

    }
}
