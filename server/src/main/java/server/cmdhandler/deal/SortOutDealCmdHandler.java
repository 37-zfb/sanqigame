package server.cmdhandler.deal;

import entity.db.UserEquipmentEntity;
import entity.db.UserPotionEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.cmdhandler.duplicate.PropsUtil;
import server.cmdhandler.task.listener.TaskPublicMethod;
import server.model.DealProps;
import server.model.PlayDeal;
import server.model.User;
import server.model.props.AbstractPropsProperty;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import server.scene.GameData;
import server.timer.state.DbUserStateTimer;
import type.PropsType;
import util.MyUtil;

import java.util.Collections;
import java.util.Map;

/**
 * @author 张丰博
 * 整理交易的物品
 */
@Component
@Slf4j
public class SortOutDealCmdHandler implements ICmdHandler<GameMsg.SortOutDealCmd> {

    @Autowired
    private TaskPublicMethod taskPublicMethod;

    @Autowired
    private DbUserStateTimer userStateTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.SortOutDealCmd sortOutDealCmd) {

        MyUtil.checkIsNull(ctx, sortOutDealCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        Map<Integer, Props> backpack = user.getBackpack();
        PlayDeal playDeal = user.getPLAY_DEAL();
        playDeal.setAgreeNumber(0);
        playDeal.getTargetUserId().set(0);

        Integer prepareMoney = playDeal.getPrepareMoney();
        user.setMoney(user.getMoney() - prepareMoney);
        log.info("用户 {} 交易出的金币: {} ;", user.getUserName(), prepareMoney);


        Map<Integer, DealProps> prepareProps = playDeal.getPrepareProps();
        for (Map.Entry<Integer, DealProps> dealPropsEntry : prepareProps.entrySet()) {
            Integer key = dealPropsEntry.getKey();
            Props props = backpack.get(key);
            if (props == null) {
                continue;
            }

            if (props.getPropsProperty().getType() == PropsType.Equipment) {
                backpack.remove(key);

                Equipment equipment = (Equipment) (props.getPropsProperty());
                UserEquipmentEntity equipmentEntity = new UserEquipmentEntity();
                equipmentEntity.setId(equipment.getId());
                userStateTimer.deleteUserEquipment(equipmentEntity);
            }

            if (props.getPropsProperty().getType() == PropsType.Potion) {
                Potion potion = (Potion) props.getPropsProperty();

                UserPotionEntity potionEntity = new UserPotionEntity();
                potionEntity.setId(potion.getId());

                if (potion.getNumber() > dealPropsEntry.getValue().getNumber()) {
                    potion.setNumber(potion.getNumber() - dealPropsEntry.getValue().getNumber());
                    potionEntity.setNumber(potion.getNumber());
                    userStateTimer.modifyUserPotion(potionEntity);
                } else {
                    backpack.remove(dealPropsEntry.getKey());
                    userStateTimer.deleteUserPotion(potionEntity);
                }
            }
            log.info("用户 {} 交易出的道具: {} {}个;", user.getUserName(), props.getName(), dealPropsEntry.getValue().getNumber());
        }


        playDeal.setPrepareMoney(0);
        playDeal.getPrepareProps().clear();

        int receiveMoney = playDeal.getReceiveMoney();
        Map<Integer, DealProps> receiveProps = playDeal.getReceiveProps();
        log.info("用户 {} 交易得到的金币: {} ;", user.getUserName(), receiveMoney);

        user.setMoney(user.getMoney() + receiveMoney);

        PropsUtil propsUtil = PropsUtil.getPropsUtil();
        for (Map.Entry<Integer, DealProps> propsEntry : receiveProps.entrySet()) {
            DealProps dealProps = propsEntry.getValue();

            propsUtil.addProps(Collections.singletonList(dealProps.getPropsId()), user, null, dealProps.getNumber());

            Props props = GameData.getInstance().getPropsMap().get(dealProps.getPropsId());
            log.info("用户 {} 交易获得的道具: {} {}个;", user.getUserName(), props.getName(), dealProps.getNumber());
        }



        GameMsg.SortOutDealResult.Builder newBuilder = GameMsg.SortOutDealResult.newBuilder();
        sortOutBackPack(newBuilder, user);
        GameMsg.SortOutDealResult sortOutDealResult = newBuilder.build();
        ctx.writeAndFlush(sortOutDealResult);

        taskPublicMethod.listener(user);

    }

    private void sortOutBackPack(GameMsg.SortOutDealResult.Builder newBuilder, User user) {
        Map<Integer, Props> backpack = user.getBackpack();
        //  背包中的道具(装备、药剂等)  , 客户端更新背包中的数据
        for (Map.Entry<Integer, Props> propsEntry : backpack.entrySet()) {
            GameMsg.Props.Builder propsResult = GameMsg.Props.newBuilder()
                    .setLocation(propsEntry.getKey())
                    .setPropsId(propsEntry.getValue().getId());

            AbstractPropsProperty propsProperty = propsEntry.getValue().getPropsProperty();
            if (propsProperty.getType() == PropsType.Equipment) {
                Equipment equipment = (Equipment) propsProperty;
                propsResult.setDurability(equipment.getDurability()).setUserPropsId(equipment.getId());
            } else if (propsProperty.getType() == PropsType.Potion) {
                Potion potion = (Potion) propsProperty;
                propsResult.setPropsNumber(potion.getNumber())
                        .setUserPropsId(potion.getId());
            }
            newBuilder.addProps(propsResult);
        }
        newBuilder.setMoney(user.getMoney());
    }


}
