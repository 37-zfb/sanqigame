package client.cmd;

import client.model.server.props.Equipment;
import client.model.server.props.Props;
import client.thread.CmdThread;
import client.model.Role;
import client.model.SceneData;
import constant.EquipmentConst;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import entity.db.UserEquipmentEntity;
import type.PropsType;

import java.util.Map;

/**
 * @author 张丰博
 */
public class RepairEquipmentResultClient implements ICmd<GameMsg.RepairEquipmentResult> {

    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.RepairEquipmentResult repairEquipmentResult) {
        if (ctx == null || repairEquipmentResult == null){
            return;
        }
        Role role = Role.getInstance();
        // 需要修理的装备id
        int userEquipmentId = repairEquipmentResult.getUserEquipmentId();
        UserEquipmentEntity[] userEquipmentArr = role.getUserEquipmentEntityArr();

        for (int i = 0; i < userEquipmentArr.length; i++) {
            if (userEquipmentArr[i] != null && userEquipmentId == userEquipmentArr[i].getId()) {
                // 修理装备，设置耐久度100
                userEquipmentArr[i].setDurability(100);
            }
        }
        Map<Integer, Props> backpackClient = role.getBackpackClient();
        for (Props props : backpackClient.values()) {

            if ( props.getPropsProperty().getType() == PropsType.Equipment && ((Equipment)props.getPropsProperty()).getId() == userEquipmentId) {
                // 修理装备，设置耐久度100
                ((Equipment)props.getPropsProperty()).setDurability(EquipmentConst.MAX_DURABILITY);
            }
        }

        CmdThread.getInstance().process(ctx,role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());

    }
}
