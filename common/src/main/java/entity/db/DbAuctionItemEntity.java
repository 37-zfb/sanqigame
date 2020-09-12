package entity.db;

import exception.CustomizeErrorCode;
import exception.CustomizeException;
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
     * 竞拍者信息
     */
    private DbBidderEntity bidder;

    /**
     * 添加竞拍者
     *
     * @param bidderEntity
     */
    public DbBidderEntity addBidder(DbBidderEntity bidderEntity) {
        if (bidder != null && bidder.getMoney() > bidderEntity.getMoney()) {
            throw new CustomizeException(CustomizeErrorCode.USER_MONEY_INSUFFICIENT);
        }

        if (bidder != null && bidder.getUserId().equals(bidderEntity.getUserId())) {
            throw new CustomizeException(CustomizeErrorCode.ALREADY_JOIN_BIDDING);
        }
        DbBidderEntity temp = bidder;
        bidder = bidderEntity;
        return temp;
    }


}
