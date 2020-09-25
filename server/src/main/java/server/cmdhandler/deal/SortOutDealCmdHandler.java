package server.cmdhandler.deal;

import entity.db.UserEquipmentEntity;
import entity.db.UserPotionEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.Deal;
import server.util.PropsUtil;
import server.cmdhandler.task.listener.TaskUtil;
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
import type.TaskType;
import util.MyUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author 张丰博
 * 整理交易的物品
 */
@Component
@Slf4j
public class SortOutDealCmdHandler implements ICmdHandler<GameMsg.SortOutDealCmd> {

    @Autowired
    private TaskUtil taskPublicMethod;

    @Autowired
    private DbUserStateTimer userStateTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.SortOutDealCmd sortOutDealCmd) {

        MyUtil.checkIsNull(ctx, sortOutDealCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        Deal deal = user.getDeal();
        if (deal == null || deal.getTargetId() == null || deal.getInitiatorId() == null) {
            return;
        }

        if (!deal.isTargetIsDetermine() || !deal.isInitiatorIsDetermine()) {
            return;
        }

        // 交易出的 金币，道具
        Integer prepareMoney = 0;
        Map<Integer, DealProps> prepareProps = null;
        if (user.getUserId() == deal.getInitiatorId()) {
            prepareMoney = user.getDeal().getInitiatorMoney();
            prepareProps = user.getDeal().getInitiatorProps();
        }
        if (user.getUserId() == deal.getTargetId()) {
            prepareMoney = user.getDeal().getTargetMoney();
            prepareProps = user.getDeal().getTargetProps();
        }

        int receiveMoney = 0;
        Map<Integer, DealProps> receiveProps = null;
        if (user.getUserId() == deal.getInitiatorId()) {
            receiveMoney = user.getDeal().getTargetMoney();
            receiveProps = user.getDeal().getTargetProps();
        }
        if (user.getUserId() == deal.getTargetId()) {
            receiveMoney = user.getDeal().getInitiatorMoney();
            receiveProps = user.getDeal().getInitiatorProps();
        }


        user.setMoney(user.getMoney() - prepareMoney);
        log.info("用户 {} 交易出的金币: {} ;", user.getUserName(), prepareMoney);

        GameMsg.SortOutDealResult.Builder newBuilder = GameMsg.SortOutDealResult.newBuilder();
        newBuilder.setRemoveMoney(prepareMoney);
        removeProps(prepareProps, user, newBuilder);


        log.info("用户 {} 交易得到的金币: {} ;", user.getUserName(), receiveMoney);
        user.setMoney(user.getMoney() + receiveMoney);
        newBuilder.setMoney(receiveMoney);
        PropsUtil propsUtil = PropsUtil.getPropsUtil();
        for (Map.Entry<Integer, DealProps> propsEntry : receiveProps.entrySet()) {
            DealProps dealProps = propsEntry.getValue();

            propsUtil.addProps(Collections.singletonList(dealProps.getPropsId()), user, newBuilder, dealProps.getNumber());

            Props props = GameData.getInstance().getPropsMap().get(dealProps.getPropsId());
            log.info("用户 {} 交易获得的道具: {} {}个;", user.getUserName(), props.getName(), dealProps.getNumber());
        }

        user.setDeal(null);
        GameMsg.SortOutDealResult sortOutDealResult = newBuilder.build();
        ctx.writeAndFlush(sortOutDealResult);

        taskPublicMethod.listener(user, TaskType.DealType.getTaskCode());

    }

    private void removeProps(Map<Integer, DealProps> prepareProps, User user, GameMsg.SortOutDealResult.Builder newBuilder) {

        if (user == null || prepareProps == null || prepareProps.size() == 0) {
            return;
        }

        Map<Integer, Props> backpack = user.getBackpack();

        for (Map.Entry<Integer, DealProps> dealPropsEntry : prepareProps.entrySet()) {
            Integer key = dealPropsEntry.getKey();
            Props props = backpack.get(key);
            if (props == null) {
                continue;
            }

            GameMsg.Props.Builder propsBuilder = GameMsg.Props.newBuilder();

            if (props.getPropsProperty().getType() == PropsType.Equipment) {
                backpack.remove(key);

                Equipment equipment = (Equipment) (props.getPropsProperty());
                UserEquipmentEntity equipmentEntity = new UserEquipmentEntity();
                equipmentEntity.setId(equipment.getId());
                userStateTimer.deleteUserEquipment(equipmentEntity);

                propsBuilder.setLocation(key)
                        .setPropsNumber(1);
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
                    backpack.remove(key);
                    userStateTimer.deleteUserPotion(potionEntity);

                }
                propsBuilder.setLocation(key)
                        .setPropsNumber(dealPropsEntry.getValue().getNumber());
            }
            newBuilder.addRemoveProps(propsBuilder);
            log.info("用户 {} 交易出的道具: {} {}个;", user.getUserName(), props.getName(), dealPropsEntry.getValue().getNumber());
        }
    }

}
