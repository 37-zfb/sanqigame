package type;

/**
 * @author 张丰博
 */
public enum  TaskType {

    /**
     * 对话类型任务
     */
    DialogueType(1,"dialogue"),
    /**
     * 击杀类型
     */
    killType(2,"kill"),
    /**
     * 通关
     */
    DuplicateType(3,"duplicate"),
    /**
     * 第一次XXX任务
     */
    FirstTime(4,"first"),
    /**
     * 此时表示任务已做完
     */
    NonTask(Integer.MAX_VALUE,"non"),

    /**
     * 当前任务是否完成
     */
    CurrTaskCompleted(1,"当前任务已完成;"),
    /**
     * 当前任务未完成
     */
    CurrTaskUnCompleted(2,"当前任务未完成"),

    ;
    private Integer taskCode;
    private String taskType;

    private TaskType(Integer taskCode,String taskType){
        this.taskCode = taskCode;
        this.taskType = taskType;
    }

    public Integer getTaskCode() {
        return taskCode;
    }

    public String getTaskType() {
        return taskType;
    }
}
