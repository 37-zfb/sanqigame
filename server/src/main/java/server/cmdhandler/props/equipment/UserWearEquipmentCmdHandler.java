package server.cmdhandler.props.equipment;

import constant.EquipmentConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import server.PublicMethod;
import server.cmdhandler.task.listener.TaskUtil;
import server.model.props.Props;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import entity.db.UserEquipmentEntity;
import server.scene.GameData;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.props.Equipment;
import server.service.UserService;
import server.timer.state.DbUserStateTimer;
import util.MyUtil;

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
    private DbUserStateTimer userStateTimer;

    @Autowired
    private TaskUtil taskPublicMethod;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserWearEquipmentCmd userWearEquipmentCmd) {
        MyUtil.checkIsNull(ctx, userWearEquipmentCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

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
                new UserEquipmentEntity(equipment.getId(), user.getUserId(), props.getId(), EquipmentConst.WEAR, equipment.getDurability());

        //去掉背包中的装备
        backpack.remove(location);

        // 添加进 装备栏;如果装备栏中有该类型的装备，则把它添加进背包，并设置未穿戴
        UserEquipmentEntity[] userEquipmentArr = user.getUserEquipmentArr();
        boolean isSuccess = false;
        for (int i = 0; i < userEquipmentArr.length; i++) {
            // 如果类型相同
            if (userEquipmentArr[i] != null
                    && equipment.getEquipmentType().getType().equals(((Equipment) propsMap.get(userEquipmentArr[i].getPropsId()).getPropsProperty()).getEquipmentType().getType())) {

                Props pro = propsMap.get(userEquipmentArr[i].getPropsId());
                Equipment equ = (Equipment) pro.getPropsProperty();

                // 把换下来的装备加入背包
                backpack.put(location, new Props(pro.getId(), pro.getName(), new Equipment(userEquipmentArr[i].getId(), pro.getId(), userEquipmentArr[i].getDurability(), equ.getDamage(), equ.getEquipmentType())));
                // 更新数据库,脱下来的装备
                userEquipmentArr[i].setIsWear(EquipmentConst.NO_WEAR);
                userEquipmentArr[i].setLocation(location);
                userStateTimer.modifyUserEquipment(userEquipmentArr[i]);

//                userService.modifyWearEquipment(userEquipmentArr[i].getId(), 0,location);
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
//        userService.modifyWearEquipment(equipment.getId(), 1,-1);
        wearEqu.setLocation(-1);
        userStateTimer.modifyUserEquipment(wearEqu);

        taskPublicMethod.listener(user);

        GameMsg.UserWearEquipmentResult equipmentResult = GameMsg.UserWearEquipmentResult.newBuilder()
                .setPropsId(wearEqu.getPropsId())
                .setLocation(location)
                .setUserEquipmentId(wearEqu.getId())
                .build();

        ctx.channel().writeAndFlush(equipmentResult);

    }
}
