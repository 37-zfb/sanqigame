package server.cmdhandler.equipment;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import server.cmdhandler.task.listener.TaskPublicMethod;
import server.model.props.Props;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import entity.db.UserEquipmentEntity;
import server.scene.GameData;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.UserManager;
import server.model.props.Equipment;
import server.service.UserService;

import java.util.Map;

/**
 * 穿装备
 *
 * @author 张丰博
 */
@Component
@Slf4j
public class UserWearEquipmentCmdHandler implements ICmdHandler<GameMsg.UserWearEquipmentCmd> {

    @Autowired
    private UserService userService;
    @Autowired
    private TaskPublicMethod taskPublicMethod;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserWearEquipmentCmd userWearEquipmentCmd) {
        if (ctx == null || userWearEquipmentCmd == null) {
            return;
        }
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        User user = UserManager.getUserById(userId);

        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();

        int location = userWearEquipmentCmd.getLocation();
        // 当前装备在 user_equipment 表中的id
        long userEquipmentId = userWearEquipmentCmd.getUserEquipmentId();

        // 背包
        Map<Integer, Props> backpack = user.getBackpack();
        // 对应的装备
        Props props = backpack.get(location);
        // 将要穿戴的装备
        Equipment equipment = (Equipment) props.getPropsProperty();
        // 背包中的装备
        // 将要穿戴的装备
        UserEquipmentEntity wearEqu =
                new UserEquipmentEntity(userEquipmentId, userId, props.getId(), 1, equipment.getDurability());

        //去掉背包中的装备
        backpack.remove(location);

        // 添加进 装备栏;如果装备栏中有该类型的装备，则把它添加进背包，并设置未穿戴
        UserEquipmentEntity[] userEquipmentArr = user.getUserEquipmentArr();
        boolean isSuccess = false;
        for (int i = 0; i < userEquipmentArr.length; i++) {
            // 如果类型相同
            if (userEquipmentArr[i] != null && equipment.getEquipmentType().getType() == ((Equipment) propsMap.get(userEquipmentArr[i].getPropsId()).getPropsProperty()).getEquipmentType().getType()) {

                Props pro = propsMap.get(userEquipmentArr[i].getPropsId());
                Equipment equ = (Equipment) pro.getPropsProperty();

                // 把换下来的装备加入背包
                backpack.put(location, new Props(pro.getId(), pro.getName(), new Equipment(userEquipmentArr[i].getId(), pro.getId(), userEquipmentArr[i].getDurability(), equ.getDamage(), equ.getEquipmentType())));
                // 更新数据库,脱下来的装备
                userService.modifyWearEquipment(userEquipmentArr[i].getId(), 0,location);
                // 穿上指定的装备
                userEquipmentArr[i] = wearEqu;
                isSuccess = true;
            }
        }
        if (!isSuccess) {
            for (int i = 0; i < userEquipmentArr.length; i++) {
                if (userEquipmentArr[i] == null) {
                    userEquipmentArr[i] = wearEqu;
                    break;
                }
            }
        }
        userService.modifyWearEquipment(equipment.getId(), 1,-1);

        taskPublicMethod.listener(user);

        GameMsg.UserWearEquipmentResult.Builder builder = GameMsg.UserWearEquipmentResult.newBuilder();
        GameMsg.UserWearEquipmentResult equipmentResult = builder.setPropsId(wearEqu.getPropsId())
                .setLocation(location)
                .setUserEquipmentId(wearEqu.getId()).build();

        ctx.channel().writeAndFlush(equipmentResult);

    }
}
