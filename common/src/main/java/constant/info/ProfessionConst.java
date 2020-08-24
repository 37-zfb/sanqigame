package constant.info;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 张丰博
 */
@Getter
@ToString
public final class ProfessionConst {
    /**
     *  初始化hp
     */
    public  int HP;
    /**
     *  初始化mp
     */
    public  int MP;

    /**
     * 初始化当前场景
     */
    public  int INIT_CURR_SCENE_ID;

    /**
     *  自动回蓝的值
     */
    public  int AUTO_RESUME_MP_VALUE;

    /**
     *  初始化金币
     */
    public  int INIT_MONEY;

    private static ProfessionConst professionConst = null;

    private ProfessionConst() {
    }

    public static ProfessionConst getProfessionConst(){
        return professionConst;
    }

    private void init(ProfessionConst professionConst){
        ProfessionConst.professionConst = professionConst;
    }

}
