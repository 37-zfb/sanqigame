package client.cmd.duplicate;

import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import model.props.Props;
import msg.GameMsg;
import scene.GameData;
import util.MyUtil;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 */
public class DuplicateFinishResultClient implements ICmd<GameMsg.DuplicateFinishResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.DuplicateFinishResult duplicateFinishResult) {
        MyUtil.checkIsNull(ctx, duplicateFinishResult);
        Role role = Role.getInstance();

        role.setMoney(duplicateFinishResult.getMoney());
        List<Integer> propsIdList = duplicateFinishResult.getPropsIdList();

        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        System.out.println("获得的道具:");
        for (Integer id : propsIdList) {
            System.out.println(propsMap.get(id).getName());
        }


    }
}
