package type;

/**
 * @author 张丰博
 */
public enum  DealInfoType {
    /**
     *  交易时，添加道具
     */
    ADD("add"),
    /**
     *  交易时，取消道具
     */
    CANCEL("cancel"),
    ;

    /**
     * 添加 or 取消
     */
    private String type;

    DealInfoType(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
