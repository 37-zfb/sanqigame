package constant.info;

import lombok.Getter;
import lombok.ToString;

/**
 * @author 张丰博
 */
@Getter
@ToString
public final class BossMonsterConst {
    /**
     * 普通攻击次数
     */
    public  int ORDINARY_ATTACK;

    private static BossMonsterConst bossMonsterConst = null;

    private BossMonsterConst() {
    }

    public static BossMonsterConst getBossMonsterConst(){
        return bossMonsterConst;
    }

    private void init(BossMonsterConst bossMonsterConst){
        BossMonsterConst.bossMonsterConst = bossMonsterConst;
    }
}
