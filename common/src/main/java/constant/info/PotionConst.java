package constant.info;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author 张丰博
 */
@Getter
@ToString
public final class PotionConst {
    /**
     *  缓慢MP、HP药剂有效时间
     */
    public  int SLOW_MP_POTION_TIME;
    public  int SLOW_HP_POTION_TIME;

    /**
     *  缓慢MP、HP药剂，每次恢复多少
     */
    public  int SLOW_MP_POTION_VALUE;
    public  int SLOW_HP_POTION_VALUE;

    /**
     *  药剂上限数量
     */
    public  int POTION_MAX_NUMBER;


    private static PotionConst potionConst = null;

    private PotionConst() {
    }

    public static PotionConst getPotionConst(){
        return potionConst;
    }

    private void init(PotionConst potionConst){
        PotionConst.potionConst = potionConst;
    }
}
