package client.cmd;

import client.model.server.props.Equipment;
import client.model.server.props.Props;
import client.scene.GameData;
import client.CmdThread;
import client.model.Role;
import client.model.SceneData;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import entity.db.UserEquipmentEntity;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 */
@Slf4j
public class UserUndoEquipmentResultClient implements ICmd<GameMsg.UserUndoEquipmentResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserUndoEquipmentResult userUndoEquipmentResult) {
        MyUtil.checkIsNull(ctx,userUndoEquipmentResult );
        Role role = Role.getInstance();
        UserEquipmentEntity[] userEquipmentEntityList = role.getUserEquipmentEntityArr();
        Map<Integer, Props> backpackClient = role.getBackpackClient();

        for (int i = 0; i < userEquipmentEntityList.length; i++) {
            if (userEquipmentEntityList[i] != null && userEquipmentEntityList[i].getId() == userUndoEquipmentResult.getUserEquipmentId()) {
                Props props = GameData.getInstance().getPropsMap().get(userEquipmentEntityList[i].getPropsId());
                Equipment equipment = (Equipment) props.getPropsProperty();
                // 添加到背包，
                backpackClient.put(userUndoEquipmentResult.getLocation(), new Props(props.getId(), props.getName(), new Equipment(userEquipmentEntityList[i].getId(), props.getId(), userEquipmentEntityList[i].getDurability(), equipment.getDamage(), equipment.getEquipmentType())));

                // 装备栏该位置设值未空
                userEquipmentEntityList[i] = null;
                break;
            }
        }

        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());

    }
}
