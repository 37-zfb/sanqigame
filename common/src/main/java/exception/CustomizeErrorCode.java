package exception;

/**
 * @author 张丰博
 */

public enum CustomizeErrorCode implements ICustomizeErrorCode {
    USER_NOT_FOUND(-413,"用户名或密码不正确;"),
    USER_EXISTS(-414,"用户名已经存在;"),
    USER_NOT_EXISTS(-415,"用户不存在"),
    DURABILITY(-450,"耐久度不足;"),
    BACKPACK_SPACE_INSUFFICIENT(-550,"背包空间不足;"),
    PROPS_REACH_LIMIT(-551,"此道具已达上限;"),
    USER_MONEY_INSUFFICIENT(-552,"用户金币不足;"),
    ALLOW_BUY_NUMBER_INSUFFICIENT(-553,"商品剩余数量不足;"),
    USER_NOT_MANAGER(-415,"用户没有被管理;")
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
