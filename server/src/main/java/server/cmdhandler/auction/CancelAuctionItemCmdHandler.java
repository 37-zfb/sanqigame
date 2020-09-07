package server.cmdhandler.auction;

import entity.db.DbAuctionItemEntity;
import entity.db.DbBidderEntity;
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
import server.timer.auction.DbAuctionTimer;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 * 取消拍卖品处理类
 */
@Component
@Slf4j
public class CancelAuctionItemCmdHandler implements ICmdHandler<GameMsg.CancelAuctionItemCmd> {

    @Autowired
    private DbAuctionTimer auctionTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.CancelAuctionItemCmd cancelAuctionItemCmd) {

        MyUtil.checkIsNull(ctx, cancelAuctionItemCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        int auctionId = cancelAuctionItemCmd.getAuctionId();

        DbAuctionItemEntity dbAuctionItemEntity = PlayAuction.removeAuctionItem(auctionId);
        if (dbAuctionItemEntity == null){
            throw new CustomizeException(CustomizeErrorCode.ITEM_NOT_FOUNT);
        }

        //取消定时器
        dbAuctionItemEntity.getScheduledFuture().cancel(true);

        AuctionUtil.sendMailBuyer(user.getUserId(), dbAuctionItemEntity.getPropsId(), dbAuctionItemEntity.getNumber(), "取消上架商品");

        //取消所有竞拍者，把钱还给他们
        DbBidderEntity bidder = dbAuctionItemEntity.getBidder();
        AuctionUtil.sendMailSeller(bidder.getUserId(), bidder.getMoney(), "竞拍物被取消");

        //删除拍卖品
        auctionTimer.deleteAuctionItem(dbAuctionItemEntity);
        auctionTimer.deleteBidder(bidder);

    }

    /**
     * 换回参与竞拍者的钱
     * @param bidderMap
     */
    private void backMoney(Map<Integer, DbBidderEntity> bidderMap){
        for (DbBidderEntity bidderEntity : bidderMap.values()) {

        }
    }

}
