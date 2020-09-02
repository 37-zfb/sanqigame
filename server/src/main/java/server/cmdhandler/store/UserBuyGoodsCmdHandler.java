package server.cmdhandler.store;

import constant.BackPackConst;
import constant.EquipmentConst;
import constant.PotionConst;
import entity.db.UserBuyGoodsLimitEntity;
import entity.db.UserEquipmentEntity;
import entity.db.UserPotionEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import server.model.store.Goods;
import server.model.props.AbstractPropsProperty;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.scene.GameData;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.UserManager;
import server.service.UserService;
import type.PropsType;
import util.MyUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public class UserBuyGoodsCmdHandler implements ICmdHandler<GameMsg.UserBuyGoodsCmd> {

    @Autowired
    private UserService userService;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserBuyGoodsCmd userBuyGoodsCmd) {

        MyUtil.checkIsNull(ctx, userBuyGoodsCmd);

        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        User user = UserManager.getUserById(userId);
        // 商品id 及 数量
        int goodsId = userBuyGoodsCmd.getGoodsId();
        int goodsNumber = userBuyGoodsCmd.getGoodsNumber();

        GameData gameData = GameData.getInstance();
        Map<Integer, Goods> goodsMap = gameData.getGoodsMap();
        Map<Integer, Props> propsMap = gameData.getPropsMap();
        // 要买的商品
        Goods goods = goodsMap.get(goodsId);
        Props props = propsMap.get(goods.getPropsId());

        GameMsg.UserBuyGoodsResult.Builder newBuilder = GameMsg.UserBuyGoodsResult.newBuilder();

        try {
            // 判断用户 钱 是否够
            if (user.getMoney() < goodsNumber * goods.getPrice()) {
                throw new CustomizeException(CustomizeErrorCode.USER_MONEY_INSUFFICIENT);
            }
            // 当前商品允许买的个数
            Integer allowBuyNumber = user.getGoodsAllowNumber().get(goodsId);
            // 购买的道具是否限购
            if (props.getPropsProperty().isLimit() == PropsType.Limit) {
                // 要购买的数量，是否允许
                if (goodsNumber > allowBuyNumber) {
                    // 此时商品剩余数量不足
                    throw new CustomizeException(CustomizeErrorCode.ALLOW_BUY_NUMBER_INSUFFICIENT);
                } else if (goods.getNumberLimit().equals(allowBuyNumber)) {

                    // 此时添加数据，进背包、进数据库
                    addProps(props.getId(), user, goodsNumber);

                    UserBuyGoodsLimitEntity userBuyGoodsLimitEntity = createEntity(user, goods, goodsNumber);
                    userService.addLimitNumber(userBuyGoodsLimitEntity);

                } else {
                    addProps(props.getId(), user, goodsNumber);

                    UserBuyGoodsLimitEntity userBuyGoodsLimitEntity = createEntity(user, goods, goodsNumber);
                    // 此时修改数量
                    userService.modifyLimitNumber(userBuyGoodsLimitEntity);
                }
                // 扣钱
                decreaseMoney(user, goodsNumber * goods.getPrice());
                // 更新服务端缓存，
                user.getGoodsAllowNumber().put(goodsId, allowBuyNumber - goodsNumber);
                newBuilder.setIsSuccess(true);
            } else if (props.getPropsProperty().isLimit() == PropsType.NoLimit) {
                // 此时先修改背包、数据库; 背包不通关的话直接出现异常;
                // 不限购, 直接加入背包，并且持久化
                addProps(props.getId(), user, goodsNumber);
                // 扣钱
                decreaseMoney(user, goods.getPrice());

                newBuilder.setIsSuccess(true);
            }

            log.info("用户: {} , 购买: {} , 数量: {}", user.getUserName(), props.getName(), goodsNumber);

            //  背包中的道具(装备、药剂等)  , 客户端更新背包中的数据
            Map<Integer, Props> backpack = user.getBackpack();
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
                    //potion.getId() 是数据库中的 user_potion中的id
                    propsResult.setPropsNumber(potion.getNumber()).setUserPropsId(potion.getId());
                }
                newBuilder.addProps(propsResult);
            }
            newBuilder.setGoodsId(goodsId).setGoodsNumber(goodsNumber);
        } catch (CustomizeException e) {
            newBuilder.setIsSuccess(false)
                    .setCode(e.getCode())
                    .setReason(e.getMessage());
        } finally {
            GameMsg.UserBuyGoodsResult build = newBuilder.build();
            ctx.writeAndFlush(build);
        }


    }


    private UserBuyGoodsLimitEntity createEntity(User user, Goods goods, Integer goodsNumber) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(new Date());

        // 当前商品允许买的个数
        Integer allowBuyNumber = user.getGoodsAllowNumber().get(goods.getId());

        UserBuyGoodsLimitEntity userBuyGoodsLimitEntity = new UserBuyGoodsLimitEntity();
        userBuyGoodsLimitEntity.setUserId(user.getUserId());
        userBuyGoodsLimitEntity.setGoodsId(goods.getId());
        userBuyGoodsLimitEntity.setDate(date);
        userBuyGoodsLimitEntity.setNumber((goods.getNumberLimit() - allowBuyNumber) + goodsNumber);

        return userBuyGoodsLimitEntity;
    }

    /**
     * 扣钱
     *
     * @param user
     * @param propsValue
     */
    private void decreaseMoney(User user, Integer propsValue) {
        user.setMoney(user.getMoney() - propsValue);
        userService.modifyMoney(user.getUserId(), user.getMoney());
    }


    /**
     * 购买商品，持久化数据库
     *
     * @param propsId 道具id
     * @param user    用户
     */
    private void addProps(Integer propsId, User user, Integer number) {
        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();

        Props props = propsMap.get(propsId);
        log.info("获得道具的id: {}", propsId);
        if (props.getPropsProperty().getType() == PropsType.Equipment) {
            PublicMethod.getInstance().addEquipment(user, props);
        } else if (props.getPropsProperty().getType() == PropsType.Potion) {
            PublicMethod.getInstance().addPotion(props, user, number);
        }


    }



}
