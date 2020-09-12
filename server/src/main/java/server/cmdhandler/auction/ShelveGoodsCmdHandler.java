package server.cmdhandler.auction;

import entity.db.DbAuctionItemEntity;
import entity.db.UserEquipmentEntity;
import entity.db.UserPotionEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.GameServer;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayAuction;
import server.model.User;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import server.timer.auction.ArriveTimeTimer;
import server.timer.auction.DbAuctionTimer;
import server.timer.state.DbUserStateTimer;
import type.PropsType;
import util.MyUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * @author 张丰博
 * 上架拍卖品
 */
@Component
@Slf4j
public class ShelveGoodsCmdHandler implements ICmdHandler<GameMsg.ShelveGoodsCmd> {

    @Autowired
    private DbAuctionTimer auctionTimer;

    @Autowired
    private DbUserStateTimer userStateTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.ShelveGoodsCmd shelveGoodsCmd) {

        MyUtil.checkIsNull(ctx, shelveGoodsCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        int location = shelveGoodsCmd.getLocation();
        int number = shelveGoodsCmd.getNumber();
        int auction = shelveGoodsCmd.getAuction();
        int price = shelveGoodsCmd.getPrice();

        Map<Integer, Props> backpack = user.getBackpack();
        Props props = backpack.get(location);

        if (props == null) {
            //道具不存在
            throw new CustomizeException(CustomizeErrorCode.PROPS_NOT_EXIST);
        }

        //移除道具
        removeProps(location, number, user);

        //构建 拍卖物
        DbAuctionItemEntity dbAuctionItemEntity = new DbAuctionItemEntity();
        dbAuctionItemEntity.setUserName(user.getUserName());
        dbAuctionItemEntity.setAuction(auction);
        dbAuctionItemEntity.setPrice(price);
        dbAuctionItemEntity.setNumber(number);
        dbAuctionItemEntity.setUserId(user.getUserId());
        dbAuctionItemEntity.setPropsId(props.getId());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 5);
        Date date = calendar.getTime();
        dbAuctionItemEntity.setDate(date);
        log.info("拍卖物 {} 到期时间: {}", props.getName(), date);

        Integer id = PlayAuction.addAuctionItem(dbAuctionItemEntity);
        dbAuctionItemEntity.setId(id);
        //添加进数据库
        auctionTimer.addAuctionItem(dbAuctionItemEntity);
        //定时器
        ScheduledFuture<?> scheduledFuture = ArriveTimeTimer.getArriveTimeTimer().process(dbAuctionItemEntity.getId(), date.getTime() - System.currentTimeMillis());
        dbAuctionItemEntity.setScheduledFuture(scheduledFuture);

        log.info("用户 {} 上架 {} {}个;", user.getUserName(), props.getName(), number);

        GameMsg.ShelveGoodsResult shelveGoodsResult = GameMsg.ShelveGoodsResult.newBuilder()
                .setLocation(location)
                .setNumber(number)
                .build();
        ctx.writeAndFlush(shelveGoodsResult);
    }

    /**
     * 移除道具
     *
     * @param location
     * @param number
     */
    private void removeProps(int location, int number, User user) {
        Map<Integer, Props> backpack = user.getBackpack();
        Props props = backpack.get(location);
        if (props.getPropsProperty().getType() == PropsType.Equipment) {

            UserEquipmentEntity userEquipmentEntity = new UserEquipmentEntity();
            userEquipmentEntity.setId(((Equipment) props.getPropsProperty()).getId());
            userStateTimer.deleteUserEquipment(userEquipmentEntity);
            backpack.remove(location);
        } else if (props.getPropsProperty().getType() == PropsType.Potion) {
            Potion potion = (Potion) props.getPropsProperty();

            if (potion.getNumber() < number) {
                throw new CustomizeException(CustomizeErrorCode.POTION_INSUFFICIENT);
            }

            UserPotionEntity userPotionEntity = new UserPotionEntity();
            userPotionEntity.setId(potion.getId());
            if (potion.getNumber() > number) {
                //修改数量
                userPotionEntity.setNumber(potion.getNumber() - number);
                potion.setNumber(potion.getNumber() - number);
                userStateTimer.modifyUserPotion(userPotionEntity);
            } else {
                //删除
                userStateTimer.deleteUserPotion(userPotionEntity);
            }
            log.info("用户 {} 减少 {} {}", user.getUserName(), number, props.getName());
        }
    }

}
