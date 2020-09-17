package type;

import java.util.Arrays;
import java.util.List;

/**
 * @author 张丰博
 */
public enum TaskType {

    /**
     * 对话类型任务
     */
    DialogueType(1, "server.cmdhandler.task.listener.DialogueTask"),
    /**
     * 击杀 XXX 怪 X 只
     */
    KillMonsterType(2, "server.cmdhandler.task.listener.KillMonsterTask"),

    /**
     * 加入XXX
     */
    JoinType(3, "server.cmdhandler.task.listener.JoinTask"),

    /**
     * 副本
     */
    DuplicateType(4, "server.cmdhandler.task.listener.ClearanceDuplicateTask"),

    /**
     * 等级
     */
    LvType(5, "server.cmdhandler.task.listener.ReachLvTask"),

    /**
     * 获得装备 XXX 个装备
     */
    GetEquipmentType(6, "server.cmdhandler.task.listener.GetEquipmentTask"),

    /**
     * 穿戴装备 XXX 个装备
     */
    WearEquipmentType(7, "server.cmdhandler.task.listener.WearEquipmentTask"),

    /**
     * 添加好友
     */
    AddFriendType(8, "server.cmdhandler.task.listener.AddFriendTask"),

    /**
     * 组队
     */
    AddTeamType(9, "server.cmdhandler.task.listener.TeamTask"),

    /**
     * 与玩家交易
     */
    DealType(10, "server.cmdhandler.task.listener.DealTask"),

    /**
     * pk中获胜
     */
    PKWin(11, "server.cmdhandler.task.listener.PkTask"),

    /**
     * 金币达到
     */
    MoneyType(12, "server.cmdhandler.task.listener.GetMoneyTask"),

    /**
     * 此时表示任务已做完
     */
    NonTask(Integer.MAX_VALUE, "non"),

    /**
     * 当前任务是否完成
     */
    CurrTaskCompleted(-1, "当前任务已完成;"),
    /**
     * 当前任务未完成
     */
    CurrTaskUnCompleted(-2, "当前任务未完成"),

    ;
    private Integer taskCode;
    private String taskType;

    private TaskType(Integer taskCode, String taskType) {
        this.taskCode = taskCode;
        this.taskType = taskType;
    }

    private static final List<Integer> CODE_LIST = Arrays.asList(3, 5, 6, 7, 8, 9, 12);

    public Integer getTaskCode() {
        return taskCode;
    }

    public String getTaskType() {
        return taskType;
    }

    /**
     *
     * @param code
     * @return
     */
    public static boolean isContain(Integer code) {
        if (code == null) {
            return false;
        }
        return CODE_LIST.contains(code);
    }

}
