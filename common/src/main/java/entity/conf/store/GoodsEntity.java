package entity.conf.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 * 商店 配置基础类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsEntity {

    /**
     *  id
     */
    private Integer id;

    /**
     *  道具id
     */
    private Integer propsId;

    /**
     *  限购数量
     */
    private Integer numberLimit;

    /**
     *  描述信息
     */
    private String info;

    /**
     *  价格
     */
    private Integer price;


}
