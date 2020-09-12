package client.cmd.guild;

import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class TakeOutMoneyResultClient implements ICmd<GameMsg.TakeOutMoneyResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.TakeOutMoneyResult takeOutMoneyResult) {

        MyUtil.checkIsNull(ctx, takeOutMoneyResult);
        Role role = Role.getInstance();
        role.setMoney(role.getMoney() + takeOutMoneyResult.getMoney());

    }
}
