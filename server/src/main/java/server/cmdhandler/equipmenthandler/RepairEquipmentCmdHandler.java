package server.cmdhandler.equipmenthandler;

import constant.EquipmentConst;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import server.model.props.Equipment;
import server.model.props.Props;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import entity.db.UserEquipmentEntity;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.UserManager;
import server.service.UserService;
import type.PropsType;

import java.util.Map;

/**
 *  修理装备
 * @author 张丰博
 */
@Component
@Slf4j
public class RepairEquipmentCmdHandler implements ICmdHandler<GameMsg.RepairEquipmentCmd> {
    @Autowired
    private UserService userService;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.RepairEquipmentCmd repairEquipmentCmd) {
        if (ctx == null || repairEquipmentCmd == null) {
            return;
        }
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        User user = UserManager.getUserById(userId);

        // 需要修理的装备id
        int userEquipmentId = repairEquipmentCmd.getUserEquipmentId();
        UserEquipmentEntity[] userEquipmentArr = user.getUserEquipmentArr();
        for (int i = 0; i < userEquipmentArr.length; i++) {
            if (userEquipmentArr[i] != null && userEquipmentId == userEquipmentArr[i].getId()) {
                // 修理装备，设置耐久度100
                userEquipmentArr[i].setDurability(EquipmentConst.MAX_DURABILITY);
                // 持久化数据库
                userService.modifyEquipmentDurability(userEquipmentId, userEquipmentArr[i].getDurability());
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
                    userService.modifyEquipmentDurability(userEquipmentId, equipment.getDurability());
                }

            }
        }

        GameMsg.RepairEquipmentResult repairEquipmentResult = GameMsg.RepairEquipmentResult.newBuilder()
                .setUserEquipmentId(userEquipmentId)
                .build();
        ctx.channel().writeAndFlush(repairEquipmentResult);

    }
}
