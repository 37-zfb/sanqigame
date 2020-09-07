package server.cmdhandler.deal;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.cmdhandler.task.listener.TaskPublicMethod;
import server.model.DealProps;
import server.model.PlayDeal;
import server.model.User;
import server.model.props.AbstractPropsProperty;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import server.scene.GameData;
import type.PropsType;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 *  整理交易的物品
 */
@Component
@Slf4j
public class SortOutDealCmdHandler implements ICmdHandler<GameMsg.SortOutDealCmd> {

    @Autowired
    private TaskPublicMethod taskPublicMethod;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.SortOutDealCmd sortOutDealCmd) {

        MyUtil.checkIsNull(ctx, sortOutDealCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        Map<Integer, Props> backpack = user.getBackpack();
        PlayDeal play_deal = user.getPLAY_DEAL();
        play_deal.setAgreeNumber(0);
        play_deal.getTargetUserId().set(0);

        Integer prepareMoney = play_deal.getPrepareMoney();
        user.setMoney(user.getMoney()-prepareMoney);
        log.info("用户 {} 交易出的金币: {} ;", user.getUserName(),prepareMoney);


        Map<Integer, DealProps> prepareProps = play_deal.getPrepareProps();
        for (Map.Entry<Integer, DealProps> dealPropsEntry : prepareProps.entrySet()) {
            Integer key = dealPropsEntry.getKey();
            Props props = backpack.get(key);
            if (props.getPropsProperty().getType() == PropsType.Equipment){
                backpack.remove(key);
            }else if (props.getPropsProperty().getType() == PropsType.Potion){
                Potion potion = (Potion) props.getPropsProperty();
                if (potion.getNumber() > dealPropsEntry.getValue().getNumber()){
                    potion.setNumber(potion.getNumber()-dealPropsEntry.getValue().getNumber());
                }else {
                    backpack.remove(dealPropsEntry.getValue().getNumber());
                }
            }
            log.info("用户 {} 交易出的道具: {} {}个;", user.getUserName(),props.getName(),dealPropsEntry.getValue().getNumber());
        }


        play_deal.setPrepareMoney(0);
        play_deal.getPrepareProps().clear();

        int receiveMoney = play_deal.getReceiveMoney();
        Map<Integer, DealProps> receiveProps = play_deal.getReceiveProps();
        log.info("用户 {} 交易得到的金币: {} ;", user.getUserName(),receiveMoney);


        user.setMoney(user.getMoney()+receiveMoney);
        PublicMethod publicMethod = PublicMethod.getInstance();
        for (Map.Entry<Integer, DealProps> propsEntry : receiveProps.entrySet()) {
            DealProps dealProps = propsEntry.getValue();
            Props props = GameData.getInstance().getPropsMap().get(dealProps.getPropsId());
            if (props.getPropsProperty().getType() == PropsType.Equipment){
                publicMethod.addEquipment(user, props);
            }else if (props.getPropsProperty().getType() == PropsType.Potion){
                publicMethod.addPotion(props, user, dealProps.getNumber());
            }
            log.info("用户 {} 交易获得的道具: {} {}个;", user.getUserName(),props.getName(),dealProps.getNumber());
        }


        GameMsg.SortOutDealResult.Builder newBuilder = GameMsg.SortOutDealResult.newBuilder();
        //  背包中的道具(装备、药剂等)  , 客户端更新背包中的数据
        for (Map.Entry<Integer, Props> propsEntry : backpack.entrySet()) {
            GameMsg.Props.Builder propsResult = GameMsg.Props.newBuilder()
                    .setLocation(propsEntry.getKey())
                    .setPropsId(propsEntry.getValue().getId());

            AbstractPropsProperty propsProperty = propsEntry.getValue().getPropsProperty();
            if (propsProperty.getType() == PropsType.Equipment) {
                Equipment equipment = (Equipment) propsProperty;
                // equipment.getId() 是数据库中的user_equipment中的id
                propsResult.setDurability(equipment.getDurability()).setUserPropsId(equipment.getId());
            } else if (propsProperty.getType() == PropsType.Potion) {
                Potion potion = (Potion) propsProperty;
                //potion.getId() 是数据库中的 user_potion中的id ,, 缺少id 出异常
                propsResult.setPropsNumber(potion.getNumber());
//                        .setUserPropsId(potion.getId());
            }
            newBuilder.addProps(propsResult);
        }
        newBuilder.setMoney(user.getMoney());

        taskPublicMethod.listener(user);

        GameMsg.SortOutDealResult sortOutDealResult = newBuilder.build();
        ctx.writeAndFlush(sortOutDealResult);
    }
}
