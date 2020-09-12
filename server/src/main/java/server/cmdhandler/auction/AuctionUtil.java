package server.cmdhandler.auction;

import com.alibaba.fastjson.JSON;
import entity.db.DbAuctionItemEntity;
import entity.db.DbBidderEntity;
import entity.db.DbSendMailEntity;
import msg.GameMsg;
import server.GameServer;
import entity.MailProps;
import server.cmdhandler.mail.MailUtil;
import server.model.User;
import server.model.UserManager;
import server.timer.auction.DbAuctionTimer;
import server.timer.mail.DbSendMailTimer;
import server.util.IdWorker;
import type.MailType;

import java.util.*;

/**
 * @author 张丰博
 * 拍卖行工具类
 */
public final class AuctionUtil {

    private static final DbSendMailTimer sendMailTimer = GameServer.APPLICATION_CONTEXT.getBean(DbSendMailTimer.class);

    private static final DbAuctionTimer auctionTimer = GameServer.APPLICATION_CONTEXT.getBean(DbAuctionTimer.class);

    private AuctionUtil() {
    }


    /**
     * 当物品上架时间到时，计算是否有竞拍者，有则把物品给最高价者
     *
     * @param auctionItemEntity
     * @param dbBidderEntity
     */
    public static void auctionResult(DbAuctionItemEntity auctionItemEntity, DbBidderEntity dbBidderEntity) {
        //下架时间到
        if (dbBidderEntity != null) {
            //出价最高者
            MailUtil.getMailUtil().sendMail(dbBidderEntity.getUserId(),
                    0,
                    "竞拍成功,获得拍卖品",
                    Collections.singletonList(new MailProps(auctionItemEntity.getPropsId(), auctionItemEntity.getNumber())));
            //删除竞拍者
            auctionTimer.deleteBidder(dbBidderEntity);

            //把钱发给拍卖者
            MailUtil.getMailUtil().sendMail(auctionItemEntity.getUserId(),
                    dbBidderEntity.getMoney(),
                    "拍卖物品卖出金币",
                    new ArrayList<>());
        } else {
            // 若没有竞拍者，此时把拍卖品返回拍卖者
            MailUtil.getMailUtil().sendMail(auctionItemEntity.getUserId(),
                    0,
                    "拍卖失败,返回拍卖品",
                    Collections.singletonList(new MailProps(auctionItemEntity.getPropsId(), auctionItemEntity.getNumber())));
        }
    }

}
