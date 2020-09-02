package entity.db;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
public class DbBidderEntity {

    /**
     * id
     */
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 拍卖品id
     */
    private Integer auctionId;

    /**
     * 钱
     */
    private Integer money;

}
