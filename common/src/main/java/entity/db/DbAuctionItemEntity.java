package entity.db;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
public class DbAuctionItemEntity {

    /**
     * id
     */
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 拍卖人姓名
     */
    private String userName;

    /**
     * 竞拍价
     */
    private Integer auction;

    /**
     * 一口价
     */
    private Integer price;

    /**
     * 拍卖品id
     */
    private Integer propsId;

    /**
     * 拍卖个数， 此字段针对药剂
     */
    private Integer number;

    /**
     * 到期时间
     */
    private Date date;

    /**
     * 调度器
     */
    private ScheduledFuture<?> scheduledFuture;


    /**
     * 竞拍者集合 ，  用户id 竞拍信息
     */
    private final Map<Integer,DbBidderEntity> BIDDER_MAP  = new ConcurrentHashMap<>();

    /**
     * 添加竞拍者
     * @param bidderEntity
     */
    public void addBidder(DbBidderEntity bidderEntity){
        if (bidderEntity != null){
            BIDDER_MAP.put(bidderEntity.getUserId(), bidderEntity);
        }
    }
    public void addListBidder(List<DbBidderEntity> dbBidderEntityList){
        if (dbBidderEntityList != null){
            for (DbBidderEntity dbBidderEntity : dbBidderEntityList) {
                addBidder(dbBidderEntity);
            }
        }
    }

}
