package server.cmdhandler.auction;

import entity.MailProps;
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
import server.cmdhandler.mail.MailUtil;
import server.model.PlayAuction;
import server.model.User;
import server.timer.auction.DbAuctionTimer;
import util.MyUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        if (dbAuctionItemEntity == null) {
            throw new CustomizeException(CustomizeErrorCode.ITEM_NOT_FOUNT);
        }

        //取消定时器
        dbAuctionItemEntity.getScheduledFuture().cancel(true);


        MailUtil.getMailUtil().sendMail(user.getUserId(),
                0,
                "取消上架商品",
                Collections.singletonList(new MailProps(dbAuctionItemEntity.getPropsId(), dbAuctionItemEntity.getNumber())));

        //取消所有竞拍者，把钱还给他们
        DbBidderEntity bidder = dbAuctionItemEntity.getBidder();
        if (bidder != null) {
            MailUtil.getMailUtil().sendMail(bidder.getUserId(),
                    bidder.getMoney(),
                    "竞拍物被取消",
                    new ArrayList<>());
        }


        //删除拍卖品
        auctionTimer.deleteAuctionItem(dbAuctionItemEntity);
        auctionTimer.deleteBidder(bidder);

    }

}
