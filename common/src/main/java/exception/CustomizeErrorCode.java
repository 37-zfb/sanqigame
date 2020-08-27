package exception;

/**
 * @author 张丰博
 */

public enum CustomizeErrorCode implements ICustomizeErrorCode {
    /**
     *  用户名或密码不正确
     */
    USER_NOT_FOUND(-413,"用户名或密码不正确;"),
    /**
     *  用户名已经存在
     */
    USER_EXISTS(-414,"用户名已经存在;"),
    /**
     *  用户不存在
     */
    USER_NOT_EXISTS(-415,"用户不存在"),
    /**
     *  耐久度不足
     */
    DURABILITY(-450,"耐久度不足;"),
    /**
     *  背包空间不足
     */
    BACKPACK_SPACE_INSUFFICIENT(-550,"背包空间不足;"),
    /**
     *  此道具已达上限
     */
    PROPS_REACH_LIMIT(-551,"此道具已达上限;"),
    /**
     *  用户金币不足
     */
    USER_MONEY_INSUFFICIENT(-552,"用户金币不足;"),
    /**
     *  商品剩余数量不足
     */
    ALLOW_BUY_NUMBER_INSUFFICIENT(-553,"商品剩余数量不足;"),
    /**
     *  用户没有被管理
     */
    USER_NOT_MANAGER(-416,"用户没有被管理;"),


    DEAL_REQUEST_ERROR(-417,"不能和自己交易;"),
    USER_NOT_DEAL_STATUS(-418,"不是交易状态;"),

    ;

    private String message;

    private Integer code;

    private CustomizeErrorCode( Integer code,String message) {
        this.message = message;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }
}
