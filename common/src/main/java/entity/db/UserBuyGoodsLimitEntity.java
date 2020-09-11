package entity.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBuyGoodsLimitEntity {

    private Long id;

    private Integer userId;

    private Integer goodsId;

    private String date;

    private Integer number;
}
