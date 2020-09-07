package entity.db;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
public class DbTaskEntity {
    /**
     * id
     */
    private Long id;

    /**
     * userId
     */
    private Integer userId;

    /**
     * 当前任务id
     */
    private Integer currTask;

    /**
     * 当前任务是否完成
     */
    private Integer currTaskCompleted;

    /**
     * 完成的任务
     */
    private Integer completedTask;

    /**
     * 击杀个数
     */
    private Integer taskProcess = -1;
}
