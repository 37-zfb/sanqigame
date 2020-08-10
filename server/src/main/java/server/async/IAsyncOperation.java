package server.async;

/**
 * @author 张丰博
 */
public interface IAsyncOperation {


    /**
     * 获取绑定 Id
     *
     * @return 绑定 Id
     */
    default int getBindId() {
        return 0;
    }

    /**
     *  执行异步操作
     */
    void doAsyn();

    /**
     *  完成异步操作
     */
    default void doFinish(){}

}
