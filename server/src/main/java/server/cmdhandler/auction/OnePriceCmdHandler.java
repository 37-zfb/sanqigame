package server.cmdhandler.auction;

import entity.db.CurrUserStateEntity;
import entity.db.DbAuctionItemEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayAuction;
import server.model.User;
import server.model.props.Props;
import server.scene.GameData;
import server.timer.auction.DbAuctionTimer;
import server.timer.mail.DbSendMailTimer;
import server.timer.state.DbUserStateTimer;
import util.MyUtil;

/**
 * @author 张丰博
 * 一口价
 */
@Component
@Slf4j
public class OnePriceCmdHandler implements ICmdHandler<GameMsg.OnePriceCmd> {

    @Autowired
    private DbSendMailTimer sendMailTimer;
    @Autowired
    private DbAuctionTimer auctionTimer;
    @Autowired
    private DbUserStateTimer userStateTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.OnePriceCmd onePriceCmd) {

        MyUtil.checkIsNull(ctx, onePriceCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        int auctionId = onePriceCmd.getAuctionId();
        DbAuctionItemEntity auctionItemEntity = PlayAuction.removeAuctionItem(auctionId);

        if (auctionItemEntity == null){
            throw new CustomizeException(CustomizeErrorCode.ITEM_NOT_FOUNT);
        }

        if (user.getMoney() < auctionItemEntity.getPrice()) {
            // 钱不够
            throw new CustomizeException(CustomizeErrorCode.USER_MONEY_INSUFFICIENT);
        }

        Props props = GameData.getInstance().getPropsMap().get(auctionItemEntity.getPropsId());
        log.info("用户 {} 获取 {}", user.getUserName(), props.getName());

        //减钱
        user.setMoney(user.getMoney() - auctionItemEntity.getPrice());
        CurrUserStateEntity userState = PublicMethod.getInstance().createUserState(user);
        userStateTimer.modifyUserState(userState);

        //删除数据,取消定时器
        auctionTimer.deleteAuctionItem(auctionItemEntity);
        auctionItemEntity.getScheduledFuture().cancel(true);

        //给购买者发邮件
        AuctionUtil.sendMailBuyer(user.getUserId(),props.getId(),auctionItemEntity.getNumber(),"购买拍卖品成功;");
        //给拍卖者发邮件
        AuctionUtil.sendMailSeller(auctionItemEntity.getUserId(),auctionItemEntity.getPrice(),"拍卖物品卖出金币");
    }

}
