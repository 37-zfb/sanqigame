package server.cmdhandler.task.listener;

import server.model.User;

/**
 * @author 张丰博
 */
public interface Task {
    /**
     * 任务执行方法
     * @param user
     */
    void taskHandle(User user);

}
