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
                    propsProperty.setNumber(propsProperty.getNumber() + propsNumber);
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


        /*
        if (role.getId() == duplicateFinishResult.getUserId()){
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
            System.out.println("副本已通关,请退出;");
//            GameMsg.UserQuitDuplicateCmd quitDuplicateCmd = GameMsg.UserQuitDuplicateCmd.newBuilder().build();
//            ctx.writeAndFlush(quitDuplicateCmd);
        }else {
            System.out.println("副本已通关,请退出;");
        }*/


    }


}
