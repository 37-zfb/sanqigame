package model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
public class GoodsLimitNumber {

    /**
     *  突变草莓 允许购买数量
     */
    private Integer strawberryAllowNumber;

    /**
     *  雷米之吻 允许购买数量
     */
    private Integer remyAllowNumber;

    /**
     * 魔力果汁 允许购买数量
     */
    private Integer magicJuiceAllowNumber;

    /**
     * 亚斯娜的三明治 允许购买数量
     */
    private Integer sandwichAllowNumber;

}
