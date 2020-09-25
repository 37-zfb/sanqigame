package client.cmd.props.equipment;

import client.cmd.ICmd;
import client.model.Role;
import client.model.server.props.Props;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 */
public class UserSellResultClient implements ICmd<GameMsg.UserSellResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserSellResult userSellResult) {

        MyUtil.checkIsNull(ctx, userSellResult);
        Role role = Role.getInstance();

        role.setMoney(userSellResult.getCurrMoney());

        Map<Integer, Props> backpackClient = role.getBackpackClient();
        backpackClient.remove(userSellResult.getLocation());

    }
}
