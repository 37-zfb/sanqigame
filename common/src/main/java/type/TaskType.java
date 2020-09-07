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
     * 加入XXX
     */
    JoinType(3,"join"),

    /**
     * 通关
     */
    DuplicateType(4,"duplicate"),

    /**
     * 第一次XXX任务
     */
    LvType(5,"first"),

    /**
     * 获得装备
     */
    GetEquipmentType(6,"getEquipment"),

    /**
     * 穿戴装备
     */
    WearEquipmentType(7,"wearEquipment"),

    /**
     * 添加好友
     */
    AddFriendType(8,"addFriend"),

    /**
     * 组队
     */
    AddTeamType(9,"addTeam"),

    /**
     * 与玩家交易
     */
    DealType(10,"deal"),

    /**
     * pk中获胜
     */
    PKWin(11,"pk"),


    /**
     * 金币达到
     */
    MoneyType(12,"money"),

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
