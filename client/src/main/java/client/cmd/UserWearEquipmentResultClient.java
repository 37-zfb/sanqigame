package client.cmd;

import client.CmdThread;
import client.model.Role;
import client.model.SceneData;
import io.netty.channel.ChannelHandlerContext;
import model.props.Props;
import msg.GameMsg;
import entity.db.UserEquipmentEntity;
import model.props.Equipment;
import scene.GameData;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 */
public class UserWearEquipmentResultClient implements ICmd<GameMsg.UserWearEquipmentResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserWearEquipmentResult userWearEquipmentResult) {
        if (ctx == null || userWearEquipmentResult == null) {
            return;
        }
        Role role = Role.getInstance();
        int propsId = userWearEquipmentResult.getPropsId();
        int location = userWearEquipmentResult.getLocation();
        int userEquipmentId = userWearEquipmentResult.getUserEquipmentId();

        UserEquipmentEntity[] userEquipmentEntityList = role.getUserEquipmentEntityArr();
        Map<Integer, Props> backpackClient = role.getBackpackClient();
        // 将要穿戴的装备
        Props props = backpackClient.get(location);
        Equipment equipment = (Equipment) props.getPropsProperty();

        //背包中的装备
        // 即将穿上的装备
        UserEquipmentEntity userEquipmentEntity = new UserEquipmentEntity(userEquipmentId, role.getId(), propsId, 1, equipment.getDurability(), location);
        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();

        // 选中的装备从背包中去掉
        backpackClient.remove(location);
        // 同类型的装备从装备栏中去掉
        boolean isSuccess = false;
        for (int i = 0; i < userEquipmentEntityList.length; i++) {
            // 如果类型相同
            if (userEquipmentEntityList[i] != null && equipment.getEquipmentType() == ((Equipment) propsMap.get(userEquipmentEntityList[i].getPropsId()).getPropsProperty()).getEquipmentType()) {
                // 换下来的装备
                Props pro = propsMap.get(userEquipmentEntityList[i].getPropsId());
                Equipment equ = (Equipment) pro.getPropsProperty();

                // 把换下来的装备加入背包
                backpackClient.put(location,new Props(pro.getId(),pro.getName(),new Equipment(equ.getId(),equ.getPropsId(),userEquipmentEntityList[i].getDurability(),
                        equ.getDamage(),equipment.getEquipmentType())));
                // 穿上指定的装备,选中的装备添加装备栏
                userEquipmentEntityList[i] = userEquipmentEntity;
                isSuccess = true;
            }
        }
        if (!isSuccess) {
            for (int i = 0; i < userEquipmentEntityList.length; i++) {
                //Equipment equipment = propsMap.get(userEquipmentEntity.getPropsId());
                if (userEquipmentEntityList[i] == null) {
                    userEquipmentEntityList[i] = userEquipmentEntity;
                    break;
                }
            }
        }


        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
    }
}
