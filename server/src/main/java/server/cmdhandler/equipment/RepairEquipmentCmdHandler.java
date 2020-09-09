package server.cmdhandler.equipment;

import constant.EquipmentConst;
import entity.db.UserEquipmentEntity;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.UserManager;
import server.model.props.Equipment;
import server.model.props.Props;
import server.scene.GameData;
import server.service.UserService;
import server.timer.state.DbUserStateTimer;
import type.PropsType;
import util.MyUtil;

import java.util.Map;

/**
 *  修理装备
 * @author 张丰博
 */
@Component
@Slf4j
public class RepairEquipmentCmdHandler implements ICmdHandler<GameMsg.RepairEquipmentCmd> {

    @Autowired
    private DbUserStateTimer userStateTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.RepairEquipmentCmd repairEquipmentCmd) {
        MyUtil.checkIsNull(ctx, repairEquipmentCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        // 需要修理的装备id
        long userEquipmentId = repairEquipmentCmd.getUserEquipmentId();
        UserEquipmentEntity[] userEquipmentArr = user.getUserEquipmentArr();
        for (int i = 0; i < userEquipmentArr.length; i++) {
            if (userEquipmentArr[i] != null && userEquipmentId == userEquipmentArr[i].getId()) {
                // 修理装备，设置耐久度100
                userEquipmentArr[i].setDurability(EquipmentConst.MAX_DURABILITY);
                // 持久化数据库
                userStateTimer.modifyUserEquipment(userEquipmentArr[i]);

                log.info("用户 {} 修理已穿戴装备 {} ",user.getUserName(),GameData.getInstance().getPropsMap().get(userEquipmentArr[i].getPropsId()).getName());
            }
        }
        Map<Integer, Props> backpack = user.getBackpack();
        for (Props props : backpack.values()) {
            if (props.getPropsProperty().getType() == PropsType.Equipment ) {
                Equipment equipment = (Equipment) props.getPropsProperty();
                if (equipment.getId() == userEquipmentId){
                    // 修理装备，设置耐久度100
                    equipment.setDurability(EquipmentConst.MAX_DURABILITY);

                    // 持久化数据库
                    UserEquipmentEntity equipmentEntity = new UserEquipmentEntity();
                    equipmentEntity.setId(equipment.getId());
                    equipmentEntity.setDurability(EquipmentConst.MAX_DURABILITY);
                    userStateTimer.modifyUserEquipment(equipmentEntity);

                    log.info("用户 {} 修理已穿戴装备 {} ",user.getUserName(),props.getName());
                }

            }
        }

        GameMsg.RepairEquipmentResult repairEquipmentResult = GameMsg.RepairEquipmentResult.newBuilder()
                .setUserEquipmentId(userEquipmentId)
                .build();
        ctx.channel().writeAndFlush(repairEquipmentResult);

    }
}
