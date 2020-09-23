package client.cmd.deal;

import client.cmd.ICmd;
import client.model.Role;
import client.model.server.props.Props;
import client.scene.GameData;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import type.DealInfoType;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserDealItemResultClient implements ICmd<GameMsg.UserDealItemResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserDealItemResult userDealItemResult) {

        MyUtil.checkIsNull(ctx, userDealItemResult);
        Role role = Role.getInstance();

        GameMsg.Props props = userDealItemResult.getProps();
        int location = props.getLocation();
        int propsNumber = props.getPropsNumber();
        int propsId = props.getPropsId();

        Props p = GameData.getInstance().getPropsMap().get(propsId);
        if (location != 0) {
            System.out.println("对方添加道具:" + p.getName() + " " + propsNumber + "个");
        }


    }
}
