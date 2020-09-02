package client.cmd;

import client.model.Role;
import client.model.server.props.Props;
import client.scene.GameData;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import entity.db.UserEquipmentEntity;

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
        long userEquipmentId = durabilityDeficiencyResult.getUserEquipmentId();
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
