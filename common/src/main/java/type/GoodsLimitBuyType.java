package type;

/**
 * @author 张丰博
 */
public enum GoodsLimitBuyType {

    /**
     *  突变草莓
     */
    Strawberry(10,10),
    /**
     *  雷米之吻
     */
    Remy(11,10),
    /**
     * 闪亮的魔力果汁
     */
    MagicJuice(12,10),
    /**
     * 亚丝娜的三明治
     */
    Sandwich(13,10),
    ;

    private Integer goodsId;


    private Integer limitNumber;

    GoodsLimitBuyType(Integer goodsId,Integer limitNumber){
        this.goodsId = goodsId;
        this.limitNumber = limitNumber;
    }

    public Integer getGoodsId() {
        return goodsId;
    }


    public Integer getLimitNumber() {
        return limitNumber;
    }
}
