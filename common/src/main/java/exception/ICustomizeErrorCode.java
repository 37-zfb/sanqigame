package exception;

/**
 * @author 张丰博
 */
public interface ICustomizeErrorCode {
    /**
     *  获取msg
     * @return
     */
    String getMessage();

    /**
     * 获取code
     * @return
     */
    Integer getCode();
}
