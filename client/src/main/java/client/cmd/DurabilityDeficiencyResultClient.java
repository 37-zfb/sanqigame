package client.cmd;

import client.model.Role;
import client.model.SceneData;
import io.netty.channel.ChannelHandlerContext;
import model.props.Props;
import msg.GameMsg;
import entity.db.UserEquipmentEntity;
import model.props.Equipment;
import scene.GameData;

import java.util.Map;

/**
 * @author 张丰博
 */
public class DurabilityDeficiencyResultClient implements ICmd<GameMsg.DurabilityDeficiencyResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.DurabilityDeficiencyResult durabilityDeficiencyResult) {
        if (ctx == null || durabilityDeficiencyResult == null){
            return;
        }

        Role role = Role.getInstance();
        int userEquipmentId = durabilityDeficiencyResult.getUserEquipmentId();
        int propsId = durabilityDeficiencyResult.getPropsId();

        Props props = GameData.getInstance().getPropsMap().get(propsId);
        UserEquipmentEntity[] userEquipmentEntityArr = role.getUserEquipmentEntityArr();
        for (int i = 0; i < userEquipmentEntityArr.length; i++) {
            if (userEquipmentEntityArr[i].getId() == userEquipmentId){
                System.out.println("===> "+props.getName()+" 耐久度: "+ userEquipmentEntityArr[i].getDurability());
            }
        }

    }
}
