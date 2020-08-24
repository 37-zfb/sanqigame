package constant.info;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author 张丰博
 */
@Getter
@ToString
public final class DuplicateConst {
    /**
     *  玩家进入副本，3秒准备时间
     */
    public  int INIT_TIME;
    /**
     *  副本出货数
     */
    public  int PROPS_NUMBER;
    /**
     *  每个boss击杀时间
     */
    public  int BOSS_TIME;
    /**
     * 用户副本通关,正常退出
     */
    public  final String USER_NORMAL_QUIT_DUPLICATE = "副本通关,正常退出;";

    /**
     * 用户副本未通过,时间超时
     */
    public  final String USER_ABNORMAL_QUIT_DUPLICATE = "副本未通关,时间超时;";

    /**
     * 用户阵亡
     */
    public  final String USER_KILLED = "用户阵亡;";


    private static DuplicateConst duplicateConst = null;

    private DuplicateConst() {
    }

    public static DuplicateConst getDuplicateConst(){
        return duplicateConst;
    }

    private void init(DuplicateConst duplicateConst){
        DuplicateConst.duplicateConst = duplicateConst;
    }

}
