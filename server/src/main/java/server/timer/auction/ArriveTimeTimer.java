package server.timer.auction;

import entity.MailProps;
import entity.db.DbAuctionItemEntity;
import server.GameServer;
import server.cmdhandler.auction.AuctionUtil;
import server.cmdhandler.mail.MailUtil;
import server.model.PlayAuction;
import util.CustomizeThreadFactory;

import java.util.Collections;
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

    public ScheduledFuture<?> process(Integer auctionId, long delayTime) {

        if (auctionId == null) {
            return null;
        }

        ScheduledFuture<?> scheduledFuture = SCHEDULED_THREAD_POOL.schedule(() -> {
            DbAuctionItemEntity auctionItemEntity = PlayAuction.removeAuctionItem(auctionId);
            if (auctionItemEntity == null) {
                // 此时拍卖品刚被买走
                return;
            }

            //是否有竞拍者
            if (auctionItemEntity.getBidder() != null) {
                //有竞拍者
                AuctionUtil.auctionResult(auctionItemEntity, auctionItemEntity.getBidder());
            } else {
                //此时拍卖物没有被买，发送邮件给拍卖人,没有竞拍者
                MailUtil.getMailUtil().sendMail(auctionItemEntity.getUserId(),
                        0,
                        "拍卖品未卖出",
                        Collections.singletonList(new MailProps( auctionItemEntity.getPropsId(), auctionItemEntity.getNumber())));
            }
            //删除拍卖品数据
            DB_AUCTION_TIMER.deleteAuctionItem(auctionItemEntity);

            //从拍卖品中移除
            PlayAuction.removeAuctionItem(auctionItemEntity.getId());
        }, delayTime, TimeUnit.MILLISECONDS);

        return scheduledFuture;
    }


}
