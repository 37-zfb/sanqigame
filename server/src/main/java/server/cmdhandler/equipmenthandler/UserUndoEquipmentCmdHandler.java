package server.cmdhandler.equipmenthandler;

import constant.BackPackConst;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import model.props.Equipment;
import model.props.Props;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import entity.db.UserEquipmentEntity;
import scene.GameData;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.UserManager;
import server.service.UserService;

import java.util.Map;

/**
 * 脱装备
 *
 * @author 张丰博
 */
@Component
@Slf4j
public class UserUndoEquipmentCmdHandler implements ICmdHandler<GameMsg.UserUndoEquipmentCmd> {

    @Autowired
    private UserService userService;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserUndoEquipmentCmd userUndoEquipmentCmd) {
        if (ctx == null || userUndoEquipmentCmd == null) {
            return;
        }
        // 获取用户
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        User user = UserManager.getUserById(userId);

        int propsId = userUndoEquipmentCmd.getPropsId();
        int userEquipmentId = userUndoEquipmentCmd.getUserEquipmentId();
        Map<Integer, Props> backpack = user.getBackpack();

        if (backpack.size() >= BackPackConst.MAX_CAPACITY) {
            log.error("背包容量已达到上限!");
            return;
        }

        UserEquipmentEntity[] userEquipmentArr = user.getUserEquipmentArr();
        for (int i = 0; i < userEquipmentArr.length; i++) {
            if (userEquipmentArr[i] != null && userEquipmentArr[i].getId() == userEquipmentId) {

                Props props = GameData.getInstance().getPropsMap().get(userEquipmentArr[i].getPropsId());
                Equipment equipment = (Equipment) props.getPropsProperty();

                // 添加到背包，
                int j = 1;
                for (; j <= BackPackConst.MAX_CAPACITY; j++) {
                    if (!backpack.keySet().contains(j)) {
                        backpack.put(j,new Props(props.getId(),props.getName(),new Equipment(userEquipmentArr[i].getId(),propsId,userEquipmentArr[i].getDurability(),equipment.getDamage(),equipment.getEquipmentType())));
                        break;
                    }
                }
                //更新数据库
                userService.modifyWearEquipment(userEquipmentArr[i].getId(), 0,j);
                // 装备栏该位置设值未空
                userEquipmentArr[i] = null;
            }
        }

        GameMsg.UserUndoEquipmentResult userUndoEquipmentResult = GameMsg.UserUndoEquipmentResult.newBuilder()
                .setPropsId(userUndoEquipmentCmd.getPropsId())
                .setUserEquipmentId(userUndoEquipmentCmd.getUserEquipmentId())
                .build();
        ctx.channel().writeAndFlush(userUndoEquipmentResult);
    }
}
