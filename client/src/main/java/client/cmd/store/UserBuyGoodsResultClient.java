package client.cmd.store;

import client.model.server.props.Equipment;
import client.model.server.props.Potion;
import client.model.server.props.Props;
import client.model.server.scene.Scene;
import client.model.server.store.Goods;
import client.scene.GameData;
import client.thread.CmdThread;
import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import io.netty.channel.ChannelHandlerContext;

import msg.GameMsg;
import type.PropsType;
import util.MyUtil;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 */
public class UserBuyGoodsResultClient implements ICmd<GameMsg.UserBuyGoodsResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserBuyGoodsResult userBuyGoodsResult) {

        MyUtil.checkIsNull(ctx, userBuyGoodsResult);
        Role role = Role.getInstance();
        Scene scene = SceneData.getInstance().getSceneMap().get(role.getCurrSceneId());

        boolean isSuccess = userBuyGoodsResult.getIsSuccess();
        if (isSuccess) {

            int goodsId = userBuyGoodsResult.getGoodsId();
            int goodsNumber = userBuyGoodsResult.getGoodsNumber();

            Map<Integer, Goods> goodsMap = GameData.getInstance().getGoodsMap();
            Goods goods = goodsMap.get(goodsId);
            Props props1 = GameData.getInstance().getPropsMap().get(goods.getPropsId());
            if (props1.getPropsProperty().isLimit() == PropsType.Limit){
                Integer allowNumber = role.getGOODS_ALLOW_NUMBER().get(goodsId);
                role.getGOODS_ALLOW_NUMBER().put(goodsId,(allowNumber-goodsNumber));
            }

            //封装背包中的物品
            List<GameMsg.Props> propsList = userBuyGoodsResult.getPropsList();
            backpack(role,propsList);

        } else {
            String reason = userBuyGoodsResult.getReason();
            System.out.println(reason+",购买失败");
        }


        CmdThread.getInstance().process(ctx, role, scene.getNpcMap().values());
    }


    private void backpack(Role role,List<GameMsg.Props> propsList){
        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
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
    }

}
