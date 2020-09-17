package server.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 张丰博
 */
@Getter
@Setter
public class PlayTask {

    /**
     * 当前执行的任务id，若为0，则当前没有任务
     */
    private Integer currTaskId;
    /**
     * 当前任务是否完成
     */
    private boolean currTaskCompleted = false;

    /**
     * 已经完成的任务id,只记录最近完成的一个;
     */
    private Integer completedTaskId;

    /**
     * 是否还有任务
     */
    private boolean isHaveTask = true;

    /**
     * 当前击杀个数
     */
    private Integer number = 0;
}
