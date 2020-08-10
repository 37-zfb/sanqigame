package type;

/**
 * @author 张丰博
 */
public enum EquipmentType {
    /**
     *  武器
     */
    Weapon("武器"),
    /**
     *  项链
     */
    Necklace("项链"),
    /**
     *  戒指
     */
    Ring("戒指"),
    /**
     *  手镯
     */
    Bracelet("手镯"),
    /**
     *  头肩
     */
    Helmet("头肩"),
    /**
     *  上衣
     */
    Coat("上衣"),
    /**
     *  下装
     */
    Pants("下装"),
    /**
     *  腰带
     */
    Belt("腰带"),
    /**
     *  鞋子
     */
    Shoe("鞋子")
    ;

    /**
     *  装备类型
     */
    private String type;

    private EquipmentType(String type) {
        this.type = type;
    }

    public String getType(){
        return type;
    }


}
