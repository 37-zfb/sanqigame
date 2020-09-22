package client.cmd.skill;

import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class ShieldReduceResultClient implements ICmd<GameMsg.ShieldReduceResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.ShieldReduceResult shieldReduceResult) {
        MyUtil.checkIsNull(ctx, shieldReduceResult);
        Role role = Role.getInstance();

        int reduceV = shieldReduceResult.getReduceV();
        role.setShieldValue(role.getShieldValue() - reduceV);
        System.out.println("护盾减少: "+reduceV+", 剩余: "+role.getShieldValue());

    }
}
