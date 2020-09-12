package client.cmd.guild;

import client.cmd.ICmd;
import client.model.Role;
import client.model.server.props.AbstractPropsProperty;
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
public class PutInPropsResultClient implements ICmd<GameMsg.PutInPropsResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.PutInPropsResult putInPropsResult) {
        MyUtil.checkIsNull(ctx, putInPropsResult);
        Role role = Role.getInstance();

        GameMsg.Props props = putInPropsResult.getProps();

        Map<Integer, Props> backpackClient = role.getBackpackClient();
        Props p = backpackClient.get(props.getLocation());
        AbstractPropsProperty propsProperty = p.getPropsProperty();
        if (propsProperty.getType() == PropsType.Equipment){
            backpackClient.remove(props.getLocation());
        }

        if (propsProperty.getType() == PropsType.Potion){
            Potion potion = (Potion)propsProperty;

            if (props.getPropsNumber() < potion.getNumber()){
                potion.setNumber(potion.getNumber() - props.getPropsNumber());
            }else {
                backpackClient.remove(props.getLocation());
            }
        }


    }
}
