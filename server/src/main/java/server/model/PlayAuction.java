package server.model;

import entity.db.DbAuctionItemEntity;
import entity.db.DbBidderEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 张丰博
 * 拍卖行
 */
@Slf4j
public final class PlayAuction {

    /**
     * 拍卖物品
     */
    private static final Map<Integer, DbAuctionItemEntity> AUCTION_ITEM = new ConcurrentHashMap<>();

    /**
     * 拍卖行添加商品
     *
     * @param auctionItem
     */
    public static Integer addAuctionItem(DbAuctionItemEntity auctionItem) {
        int id = 0;
        if (auctionItem != null && auctionItem.getId() == null) {
            Optional<Integer> max = AUCTION_ITEM.keySet().stream().max(Comparator.comparingInt(o -> o));
            id = max.orElse(1);
            for (int i = 1; i <= (id + 1); i++) {
                if (!AUCTION_ITEM.keySet().contains(i)) {
                    id = i;
                    break;
                }
            }
            auctionItem.setId(id);
            AUCTION_ITEM.put(id, auctionItem);
        }else if (auctionItem != null && auctionItem.getId() != null){
            AUCTION_ITEM.put(auctionItem.getId(), auctionItem);
        }
        return id;
    }

    public static DbAuctionItemEntity getAuctionItemById(Integer id){
        return AUCTION_ITEM.get(id);
    }


    /**
     * 添加竞拍者
     */
    public static DbBidderEntity addBidder(DbBidderEntity dbBidderEntity) {
        DbAuctionItemEntity dbAuctionItemEntity = AUCTION_ITEM.get(dbBidderEntity.getAuctionId());

        if (dbAuctionItemEntity == null){
            throw new CustomizeException(CustomizeErrorCode.ITEM_NOT_FOUNT);
        }

        return dbAuctionItemEntity.addBidder(dbBidderEntity);
    }

    /**
     * 拍卖行取消商品
     *
     * @param id
     * @return
     */
    public static DbAuctionItemEntity removeAuctionItem(Integer id) {
        DbAuctionItemEntity auctionItemEntity = null;
        if (id != null) {
            auctionItemEntity = AUCTION_ITEM.get(id);
            AUCTION_ITEM.remove(id);
        }
        return auctionItemEntity;
    }


    /**
     * 商品集合
     *
     * @return
     */
    public static Collection<DbAuctionItemEntity> listAuctionItem() {
        return AUCTION_ITEM.values();
    }

}
