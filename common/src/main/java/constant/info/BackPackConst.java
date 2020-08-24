package constant.info;

import lombok.Getter;
import lombok.ToString;

/**
 * @author 张丰博
 */
@Getter
@ToString
public final class BackPackConst {

    /**
     * 背包最大容量
     */
    private int MAX_CAPACITY;

    private static BackPackConst backPackConst = null;

    private BackPackConst() {
    }

    public static BackPackConst getBackPackConst(){
        return backPackConst;
    }

    private void init(BackPackConst backPackConst){
        BackPackConst.backPackConst = backPackConst;
    }

}
