package client.cmd.duplicate;

import client.cmd.ICmd;
import client.model.Role;
import constant.EquipmentConst;
import entity.db.UserEquipmentEntity;
import io.netty.channel.ChannelHandlerContext;
import model.props.AbstractPropsProperty;
import model.props.Equipment;
import model.props.Potion;
import model.props.Props;
import msg.GameMsg;
import scene.GameData;
import type.PropsType;
import util.MyUtil;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 * 副本结束，
 */
public class DuplicateFinishResultClient implements ICmd<GameMsg.DuplicateFinishResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.DuplicateFinishResult duplicateFinishResult) {
        MyUtil.checkIsNull(ctx, duplicateFinishResult);
        Role role = Role.getInstance();
        // 减耐久度
        role.decreaseDurability();
        role.setMoney(duplicateFinishResult.getMoney());
        List<Integer> propsIdList = duplicateFinishResult.getPropsIdList();
        int money = duplicateFinishResult.getMoney();

        role.setMoney(role.getMoney() + money);
        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        System.out.println("获得的道具:");
        for (Integer id : propsIdList) {
            System.out.println(propsMap.get(id).getName());
        }

        //封装背包中的物品
        List<GameMsg.Props> propsList = duplicateFinishResult.getPropsList();
        Map<Integer, Props> backpackClient = role.getBackpackClient();
        for (GameMsg.Props props : propsList) {
            Props pro = propsMap.get(props.getPropsId());
            if (pro.getPropsProperty().getType() == PropsType.Equipment){
                Equipment equipment = (Equipment) pro.getPropsProperty();

                Equipment propsProperty =
                        //props.getUserPropsId() 是 表 user_equipment 中的id
                        new Equipment(props.getUserPropsId(),
                                equipment.getPropsId(),
                                props.getDurability(),
                                equipment.getDamage(),
                                equipment.getEquipmentType());

                backpackClient.put(props.getLocation(), new Props(props.getPropsId(), pro.getName(), propsProperty));
            }else if (pro.getPropsProperty().getType() == PropsType.Potion){
                Potion potion = (Potion) pro.getPropsProperty();

                Potion propsProperty =
                        //props.getUserPropsId() 是 表 user_potion 中的id
                        new Potion(props.getUserPropsId(),
                                potion.getPropsId(),
                                potion.getCdTime(),
                                potion.getInfo(),
                                potion.getResumeFigure(),
                                potion.getPercent(),
                                props.getPropsNumber());

                backpackClient.put(props.getLocation(),new Props(props.getPropsId(),pro.getName(), propsProperty));
            }
        }


        GameMsg.UserQuitDuplicateCmd quitDuplicateCmd = GameMsg.UserQuitDuplicateCmd.newBuilder().build();
        ctx.writeAndFlush(quitDuplicateCmd);

    }



}
