package constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 张丰博
 *
 * 副本常量
 *
 */
public interface DuplicateConst {
    /**
     *  玩家进入副本，3秒准备时间
     */
    int INIT_TIME = 3000;

    /**
     *  副本出货数
     */
    int DUPLICATE_REWARD_NUMBER = 6;

    /**
     *  每个boss击杀时间
     */
    long BOSS_TIME = 60000;

    /**
     *  用户副本通关,正常退出
     */
    String USER_NORMAL_QUIT_DUPLICATE = "副本通关,正常退出;";

    /**
     *  用户副本未通过,时间超时
     */
    String USER_ABNORMAL_QUIT_DUPLICATE = "副本未通关,时间超时;";

    /**
     *  用户阵亡
     */
    String USER_KILLED = "用户阵亡;";


    /**
     * 副本通关后，奖励的经验
     */
    int DUPLICATE_EXPERIENCE = 20000;

    /**
     * 道具个数
     */
    int PROPS_NUMBER = 1;

}
