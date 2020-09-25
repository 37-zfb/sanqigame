package server.cmdhandler.props.equipment;

import entity.db.UserEquipmentEntity;
import entity.db.UserPotionEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import server.timer.state.DbUserStateTimer;
import type.PropsType;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 * 卖 道具
 */
@Component
@Slf4j
public class UserSellCmdHandler implements ICmdHandler<GameMsg.UserSellCmd> {

    @Autowired
    private DbUserStateTimer userStateTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserSellCmd userSellCmd) {

        MyUtil.checkIsNull(ctx, userSellCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        int location = userSellCmd.getLocation();
        Map<Integer, Props> backpack = user.getBackpack();

        Props props = backpack.get(location);
        if (props == null) {
            return;
        }

        user.setMoney(user.getMoney() + 1000);
        backpack.remove(location);

        //删除道具
        if (props.getPropsProperty().getType() == PropsType.Equipment) {
            Equipment equipment = (Equipment) (props.getPropsProperty());
            UserEquipmentEntity equipmentEntity = new UserEquipmentEntity();
            equipmentEntity.setId(equipment.getId());
            userStateTimer.deleteUserEquipment(equipmentEntity);
        }

        if (props.getPropsProperty().getType() == PropsType.Potion) {
            Potion potion = (Potion) props.getPropsProperty();
            UserPotionEntity potionEntity = new UserPotionEntity();
            potionEntity.setId(potion.getId());
            userStateTimer.deleteUserPotion(potionEntity);

        }


        GameMsg.UserSellResult userSellResult = GameMsg.UserSellResult.newBuilder()
                .setLocation(location)
                .setCurrMoney(user.getMoney())
                .build();
        ctx.writeAndFlush(userSellResult);
    }
}
