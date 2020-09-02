package server.timer.auction;

import com.alibaba.fastjson.JSON;
import entity.db.DbAuctionItemEntity;
import entity.db.DbSendMailEntity;
import msg.GameMsg;
import server.GameServer;
import server.cmdhandler.auction.AuctionUtil;
import server.model.MailProps;
import server.model.PlayAuction;
import server.model.User;
import server.model.UserManager;
import server.service.MailService;
import type.MailType;
import util.CustomizeThreadFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 * 拍卖品到时
 */
public final class ArriveTimeTimer {

    private ArriveTimeTimer() {
    }

    private final DbAuctionTimer DB_AUCTION_TIMER = GameServer.APPLICATION_CONTEXT.getBean(DbAuctionTimer.class);

    private static final ArriveTimeTimer ARRIVE_TIME_TIMER = new ArriveTimeTimer();

    public static final ArriveTimeTimer getArriveTimeTimer() {
        return ARRIVE_TIME_TIMER;
    }

    private final ScheduledThreadPoolExecutor SCHEDULED_THREAD_POOL = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("拍卖物品到期;")
    );

    public void process(DbAuctionItemEntity auctionItemEntity, long delayTime) {

        ScheduledFuture<?> scheduledFuture = SCHEDULED_THREAD_POOL.schedule(() -> {
            //是否有竞拍者
            if (auctionItemEntity.getBIDDER_MAP().size() > 0){
                //有竞拍者
                AuctionUtil.auctionResult(auctionItemEntity,auctionItemEntity.getBIDDER_MAP().values());
            }else {
                //此时拍卖物没有被买，发送邮件给拍卖人,没有竞拍者
                AuctionUtil.sendMailBuyer(auctionItemEntity.getUserId(), auctionItemEntity.getPropsId(), auctionItemEntity.getNumber(), "拍卖品未卖出");
            }
            //删除拍卖品数据
            DB_AUCTION_TIMER.deleteAuctionItem(auctionItemEntity);

            //从拍卖品中移除
            PlayAuction.removeAuctionItem(auctionItemEntity.getId());
        }, delayTime, TimeUnit.MILLISECONDS);

        auctionItemEntity.setScheduledFuture(scheduledFuture);
    }


}
