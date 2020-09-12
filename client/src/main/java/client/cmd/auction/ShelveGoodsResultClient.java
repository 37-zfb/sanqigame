package client.cmd.auction;

import client.cmd.ICmd;
import client.model.Role;
import client.model.server.props.Potion;
import client.model.server.props.Props;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import type.PropsType;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 */
public class ShelveGoodsResultClient implements ICmd<GameMsg.ShelveGoodsResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.ShelveGoodsResult shelveGoodsResult) {

        MyUtil.checkIsNull(ctx, shelveGoodsResult);
        Role role = Role.getInstance();

        int location = shelveGoodsResult.getLocation();
        int number = shelveGoodsResult.getNumber();

        Map<Integer, Props> backpackClient = role.getBackpackClient();
        Props props = backpackClient.get(location);
        if (props.getPropsProperty().getType() == PropsType.Potion) {
            Potion potion = (Potion) props.getPropsProperty();
            if (potion.getNumber() > number) {
                potion.setNumber(potion.getNumber() - number);
            } else {
                backpackClient.remove(location);
            }
        } else {
            backpackClient.remove(location);
        }

    }
}
