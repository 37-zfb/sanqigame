package server.cmdhandler.store;

import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.util.PropsUtil;
import server.model.User;
import server.model.props.Props;
import server.model.store.Goods;
import server.scene.GameData;
import server.service.StoreService;
import type.PropsType;
import util.MyUtil;

import java.util.Arrays;
import java.util.Map;

/**
 * @author 张丰博
 * 用户购买商品
 */
@Component
@Slf4j
public class UserBuyGoodsCmdHandler implements ICmdHandler<GameMsg.UserBuyGoodsCmd> {



    @Autowired
    private StoreService storeService;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserBuyGoodsCmd userBuyGoodsCmd) {

        MyUtil.checkIsNull(ctx, userBuyGoodsCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        // 商品id 及 数量
        int goodsId = userBuyGoodsCmd.getGoodsId();
        int goodsNumber = userBuyGoodsCmd.getGoodsNumber();
        if (goodsNumber == 0){
            return;
        }

        GameData gameData = GameData.getInstance();
        Map<Integer, Goods> goodsMap = gameData.getGoodsMap();
        Map<Integer, Props> propsMap = gameData.getPropsMap();
        // 要买的商品
        Goods goods = goodsMap.get(goodsId);
        if (goods == null){
            throw new CustomizeException(CustomizeErrorCode.GOODS_NOT_EXIST);
        }

        Props props = propsMap.get(goods.getPropsId());
        if (props == null){
            throw new CustomizeException(CustomizeErrorCode.PROPS_NOT_EXIST);
        }

        // 判断用户 钱 是否够
        if (user.getMoney() < goodsNumber * goods.getPrice()) {
            throw new CustomizeException(CustomizeErrorCode.USER_MONEY_INSUFFICIENT);
        }

        GameMsg.UserBuyGoodsResult.Builder newBuilder = GameMsg.UserBuyGoodsResult.newBuilder();

        // 当前商品允许买的个数
        Integer allowBuyNumber = user.getGoodsAllowNumber().get(goodsId);
        // 购买的道具是否限购
        if (props.getPropsProperty().isLimit() == PropsType.Limit) {
            // 要购买的数量，是否允许
            if (goodsNumber > allowBuyNumber) {
                // 此时商品剩余数量不足
                throw new CustomizeException(CustomizeErrorCode.ALLOW_BUY_NUMBER_INSUFFICIENT);
            }

            // 更新服务端缓存，
            user.getGoodsAllowNumber().put(goodsId, allowBuyNumber - goodsNumber);
            // 扣钱
            user.setMoney(user.getMoney() - goodsNumber*goods.getPrice());

            if (goods.getNumberLimit().equals(allowBuyNumber)) {
                //此时用户还没有购买过
                PropsUtil.getPropsUtil().addProps(Arrays.asList(props.getId()),user,newBuilder,goodsNumber);
                storeService.addGoodsBuyNumber(user, goods);
            } else {
                //此时之前已购买过
                PropsUtil.getPropsUtil().addProps(Arrays.asList(props.getId()),user,newBuilder,goodsNumber);
                storeService.modifyGoodsBuyNumber(user, goods);
            }


        } else if (props.getPropsProperty().isLimit() == PropsType.NoLimit) {
            // 不限购, 直接加入背包，并且持久化
            PropsUtil.getPropsUtil().addProps(Arrays.asList(props.getId()),user,newBuilder,goodsNumber);
            // 扣钱

            user.setMoney(user.getMoney() - goodsNumber*goods.getPrice());
        }

        log.info("用户: {} , 购买: {} , 数量: {}", user.getUserName(), props.getName(), goodsNumber);

        newBuilder.setGoodsId(goodsId).setGoodsNumber(goodsNumber);

        GameMsg.UserBuyGoodsResult build = newBuilder.build();
        ctx.writeAndFlush(build);
    }








}
