package server.cmdhandler.equipment;

import constant.BackPackConst;
import constant.EquipmentConst;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import server.PublicMethod;
import server.model.props.Equipment;
import server.model.props.Props;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import entity.db.UserEquipmentEntity;
import server.scene.GameData;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.timer.state.DbUserStateTimer;
import util.MyUtil;

import java.util.Map;

/**
 * 脱装备
 * @author 张丰博
 */
@Component
@Slf4j
public class UserUndoEquipmentCmdHandler implements ICmdHandler<GameMsg.UserUndoEquipmentCmd> {

    @Autowired
    private DbUserStateTimer userStateTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserUndoEquipmentCmd userUndoEquipmentCmd) {
        MyUtil.checkIsNull(ctx, userUndoEquipmentCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        int propsId = userUndoEquipmentCmd.getPropsId();
        long userEquipmentId = userUndoEquipmentCmd.getUserEquipmentId();
        Map<Integer, Props> backpack = user.getBackpack();

        if (backpack.size() >= BackPackConst.MAX_CAPACITY) {
            log.error("背包容量已达到上限!");
            throw new CustomizeException(CustomizeErrorCode.BACKPACK_SPACE_INSUFFICIENT);
        }

        UserEquipmentEntity[] userEquipmentArr = user.getUserEquipmentArr();
        int location = 0;
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
                userEquipmentArr[i].setIsWear(EquipmentConst.NO_WEAR);
                userEquipmentArr[i].setLocation(j);
                userStateTimer.modifyUserEquipment(userEquipmentArr[i]);
                location = j;

                // 装备栏该位置设值未空
                userEquipmentArr[i] = null;
                break;
            }
        }

        GameMsg.UserUndoEquipmentResult userUndoEquipmentResult = GameMsg.UserUndoEquipmentResult.newBuilder()
                .setPropsId(userUndoEquipmentCmd.getPropsId())
                .setUserEquipmentId(userUndoEquipmentCmd.getUserEquipmentId())
                .setLocation(location)
                .build();
        ctx.channel().writeAndFlush(userUndoEquipmentResult);
    }
}
