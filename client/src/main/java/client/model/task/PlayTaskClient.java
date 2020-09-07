package client.model.task;

import lombok.Data;

/**
 * @author 张丰博
 */
@Data
public class PlayTaskClient {

    /**
     * 当前任务id
     */
    private Integer currTaskId;

    /**
     * 当前任务是否完成
     */
    private boolean isCompleted;

}
