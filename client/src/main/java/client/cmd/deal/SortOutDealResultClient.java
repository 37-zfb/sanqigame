package client.cmd.deal;

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
 */
public class SortOutDealResultClient implements ICmd<GameMsg.SortOutDealResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.SortOutDealResult sortOutDealResult) {
        MyUtil.checkIsNull(ctx, sortOutDealResult);
        Role role = Role.getInstance();

        role.setMoney(sortOutDealResult.getMoney());
        //封装背包中的物品
        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        List<GameMsg.Props> propsList = sortOutDealResult.getPropsList();
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
        System.out.println("整理背包完毕;");
    }
}
