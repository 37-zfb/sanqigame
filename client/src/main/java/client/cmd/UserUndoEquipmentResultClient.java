package client.cmd;

import client.CmdThread;
import client.model.Role;
import client.model.SceneData;
import constant.BackPackConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import model.props.Equipment;
import model.props.Props;
import msg.GameMsg;
import entity.db.UserEquipmentEntity;
import scene.GameData;

import java.util.Map;

/**
 * @author 张丰博
 */
@Slf4j
public class UserUndoEquipmentResultClient implements ICmd<GameMsg.UserUndoEquipmentResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserUndoEquipmentResult userUndoEquipmentResult) {
        if (ctx == null || userUndoEquipmentResult == null) {
            return;
        }
        Role role = Role.getInstance();
        UserEquipmentEntity[] userEquipmentEntityList = role.getUserEquipmentEntityArr();
        Map<Integer, Props> backpackClient = role.getBackpackClient();

        for (int i = 0; i < userEquipmentEntityList.length; i++) {
            if (userEquipmentEntityList[i] != null && userEquipmentEntityList[i].getId() == userUndoEquipmentResult.getUserEquipmentId()) {
                Props props = GameData.getInstance().getPropsMap().get(userEquipmentEntityList[i].getPropsId());
                Equipment equipment = (Equipment) props.getPropsProperty();
                // 添加到背包，
                for (int j = 1; j <= BackPackConst.MAX_CAPACITY; j++) {
                    if (!backpackClient.keySet().contains(j)) {
                        backpackClient.put(j,new Props(props.getId(),props.getName(),new Equipment(userEquipmentEntityList[i].getId(),props.getId(),userEquipmentEntityList[i].getDurability(),equipment.getDamage(),equipment.getEquipmentType())));
                        break;
                    }
                }
                // 添加到背包，
                // 装备栏该位置设值未空
                userEquipmentEntityList[i] = null;
            }
        }

        CmdThread.getInstance().process(ctx,role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());

    }
}
