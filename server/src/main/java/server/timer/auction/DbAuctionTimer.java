package server.timer.auction;

import entity.db.DbAuctionItemEntity;
import entity.db.DbBidderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.cmdhandler.auction.AuctionUtil;
import server.model.PlayAuction;
import server.model.User;
import server.model.UserManager;
import server.service.AuctionService;
import util.CustomizeThreadFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author 张丰博
 * 持久化拍卖行信息
 */
@Component
@Slf4j
public final class DbAuctionTimer {

    @Autowired
    private AuctionService auctionService;

    public DbAuctionTimer() {
        persistenceDate();
    }

    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("拍卖行持久化数据库;")
    );

    /**
     * 拍卖品
     */
    private final Map<Integer, DbAuctionItemEntity> addAuctionItem = new ConcurrentHashMap<>();
    private final Map<Integer, DbAuctionItemEntity> modifyAuctionItem = new ConcurrentHashMap<>();
    private final Map<Integer, DbAuctionItemEntity> deleteAuctionItem = new ConcurrentHashMap<>();

    /**
     * 竞拍者
     */
    private final List<DbBidderEntity> addBidder = new CopyOnWriteArrayList<>();
    private final List<DbBidderEntity> modifyBidder = new CopyOnWriteArrayList<>();
    private final List<DbBidderEntity> deleteBidder = new CopyOnWriteArrayList<>();

    /**
     * 添加拍卖品
     *
     * @param auctionItemEntity
     */
    public void addAuctionItem(DbAuctionItemEntity auctionItemEntity) {
        if (auctionItemEntity != null) {
            addAuctionItem.put(auctionItemEntity.getId(), auctionItemEntity);
        }
    }

    /**
     * 修改拍卖品
     *
     * @param auctionItemEntity
     */
    public void modifyAuctionItem(DbAuctionItemEntity auctionItemEntity) {
        if (auctionItemEntity != null) {
            modifyAuctionItem.put(auctionItemEntity.getId(), auctionItemEntity);
        }
    }

    /**
     * 删除拍卖品
     *
     * @param auctionItemEntity
     */
    public void deleteAuctionItem(DbAuctionItemEntity auctionItemEntity) {
        if (auctionItemEntity != null) {
            deleteAuctionItem.put(auctionItemEntity.getId(), auctionItemEntity);
        }
    }


    /**
     * 添加竞拍者
     *
     * @param bidderEntity
     */
    public void addBidder(DbBidderEntity bidderEntity) {
        if (bidderEntity != null) {
            addBidder.add(bidderEntity);
        }
    }

    /**
     * 修改竞拍者
     *
     * @param bidderEntity
     */
    public void modifyBidder(DbBidderEntity bidderEntity) {
        if (bidderEntity != null) {
            modifyBidder.add(bidderEntity);
        }
    }

    /**
     * 删除竞拍者
     *
     * @param bidderEntity
     */
    public void deleteBidder(DbBidderEntity bidderEntity) {
        if (bidderEntity != null) {
            deleteBidder.add(bidderEntity);
        }
    }


    private void persistenceDate() {
        scheduledThreadPool.scheduleWithFixedDelay(() -> {
            try {
                //增加拍卖品
                if (addAuctionItem.size() != 0) {
                    auctionService.addAuctionItemBatch(addAuctionItem.values());
                    addAuctionItem.clear();
                    log.info("添加拍卖品;");
                }
                //修改拍卖品信息
                if (modifyAuctionItem.size() != 0) {
                    auctionService.modifyAuctionItemBatch(modifyAuctionItem.values());
                    modifyAuctionItem.clear();
                    log.info("修改拍卖品信息;");
                }
                if (deleteAuctionItem.size() != 0) {
                    auctionService.deleteAuctionItemBatch(deleteAuctionItem.values());
                    deleteAuctionItem.clear();
                    log.info("删除拍卖品;");
                }


                //添加竞拍者
                if (addBidder.size() != 0) {
                    auctionService.addBidderBatch(addBidder);
                    addBidder.clear();
                    log.info("添加竞拍者;");
                }

                //修改竞拍者
                if (modifyBidder.size() != 0) {
                    auctionService.modifyBidderBatch(modifyBidder);
                    modifyBidder.clear();
                    log.info("修改竞拍者;");
                }

                //删除竞拍者
                if (deleteBidder.size() != 0) {
                    auctionService.deleteBidderBatch(deleteBidder);
                    deleteBidder.clear();
                    log.info("删除竞拍者;");
                }


            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }


        }, 30, 30, TimeUnit.SECONDS);
    }

    public void init() {

        List<DbAuctionItemEntity> auctionItemEntityList = auctionService.listAuctionItem();

        for (DbAuctionItemEntity auctionItemEntity : auctionItemEntityList) {
            //判断当前时间，是否应该下架拍卖品
            Date auctionItemDate = auctionItemEntity.getDate();
            Date currDate = new Date();

            List<DbBidderEntity> bidderEntityList = auctionService.listBidder(auctionItemEntity.getId());
            if (currDate.getTime() >= auctionItemDate.getTime()) {
                //此时竞价结束
                AuctionUtil.auctionResult(auctionItemEntity,bidderEntityList.get(0));
            } else {
                //此时竞价没有结束
                if (bidderEntityList != null && bidderEntityList.size() != 0) {
                    for (DbBidderEntity bidderEntity : bidderEntityList) {
                        auctionItemEntity.addBidder(bidderEntity);
                    }
                }
                PlayAuction.addAuctionItem(auctionItemEntity);
                //启动定时器
                ScheduledFuture<?> scheduledFuture = ArriveTimeTimer.getArriveTimeTimer().process(auctionItemEntity.getId(), auctionItemDate.getTime() - currDate.getTime());
                auctionItemEntity.setScheduledFuture(scheduledFuture);
            }
        }

    }




}
