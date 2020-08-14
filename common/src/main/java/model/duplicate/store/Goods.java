package model.duplicate.store;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 *  商品模型类
 */
@Data
@NoArgsConstructor
public class Goods {
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

    public Goods(Integer id,Integer propsId,Integer numberLimit,String info,Integer price){
        this.id = id;
        this.propsId = propsId;
        this.numberLimit = numberLimit;
        this.info = info;
        this.price = price;
    }

}
