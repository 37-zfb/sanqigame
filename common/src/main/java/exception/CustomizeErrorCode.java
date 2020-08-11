package exception;

/**
 * @author 张丰博
 */

public enum CustomizeErrorCode implements ICustomizeErrorCode {
    USER_NOT_FOUND(-413,"用户名或密码不正确;"),
    USER_EXISTS(-414,"用户名已经存在;"),
    DURABILITY(-450,"耐久度不足;");

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
