package client.cmd.deal;

import client.cmd.ICmd;
import client.model.Role;
import client.model.server.props.Props;
import client.scene.GameData;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserCancelDealItemResultClient implements ICmd<GameMsg.UserCancelDealItemResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserCancelDealItemResult userCancelDealItemResult) {
        MyUtil.checkIsNull(ctx, userCancelDealItemResult);
        Role role = Role.getInstance();

        GameMsg.Props props = userCancelDealItemResult.getProps();
        int location = props.getLocation();
        int propsNumber = props.getPropsNumber();
        int propsId = props.getPropsId();
        Props p = GameData.getInstance().getPropsMap().get(propsId);

        if (location != 0){
            System.out.println("对方取消道具:" + p.getName() + " " + propsNumber + "个");
        }

    }
}
