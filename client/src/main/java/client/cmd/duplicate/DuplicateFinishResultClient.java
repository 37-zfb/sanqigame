package client.cmd.duplicate;

import client.cmd.ICmd;
import client.model.Role;
import client.model.server.props.Equipment;
import client.model.server.props.Potion;
import client.model.server.props.Props;
import client.scene.GameData;
import io.netty.channel.ChannelHandlerContext;

import msg.GameMsg;
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


        int rewardMoney = duplicateFinishResult.getMoney();
        List<GameMsg.Props> rewardProps = duplicateFinishResult.getPropsList();

        System.out.println("获得金币: " + rewardMoney);
        role.setMoney(role.getMoney() + rewardMoney);

        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        Map<Integer, Props> backpackClient = role.getBackpackClient();

        System.out.println("获得道具: ");
        for (GameMsg.Props props : rewardProps) {
            int location = props.getLocation();
            int propsId = props.getPropsId();
            int propsNumber = props.getPropsNumber();

            Props pro = propsMap.get(propsId);
            System.out.println(pro.getName());

            if (pro.getPropsProperty().getType() == PropsType.Equipment) {

                Equipment equipment = (Equipment) pro.getPropsProperty();

                Equipment propsProperty =
                        //props.getUserPropsId() 是 表 user_equipment 中的id
                        new Equipment(props.getUserPropsId(),
                                equipment.getPropsId(),
                                props.getDurability(),
                                equipment.getDamage(),
                                equipment.getEquipmentType());

                backpackClient.put(location, new Props(pro.getId(), pro.getName(), propsProperty));
            } else if (pro.getPropsProperty().getType() == PropsType.Potion) {
                Props p = backpackClient.get(location);

                if (p != null) {
                    //已存在
                    Potion propsProperty = (Potion) p.getPropsProperty();
                    propsProperty.setNumber(propsNumber);
                    continue;
                }

                //不存在
                Potion potion = (Potion) pro.getPropsProperty();
                Potion propsProperty =
                        new Potion(props.getUserPropsId(),
                                potion.getPropsId(),
                                props.getDurability(),
                                potion.getInfo(),
                                potion.getResumeFigure(),
                                potion.getPercent(),
                                propsNumber);

                backpackClient.put(location, new Props(pro.getId(), pro.getName(), propsProperty));
            }

        }
    }


}
