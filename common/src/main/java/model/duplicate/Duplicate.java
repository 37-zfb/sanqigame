package model.duplicate;

import constant.DuplicateConst;
import lombok.Data;
import lombok.NoArgsConstructor;
import type.DuplicateType;

import java.util.*;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
public class Duplicate {

    /**
     * 副本id
     */
    private Integer id;

    /**
     * 副本名称
     */
    private String name;

    /**
     * 进入副本后开始攻击时间
     */
    private long startTime;

    /**
     * 当前 boss
     */
    private BossMonster currBossMonster;

    /**
     * 通关副本后，获得的奖励道具id集合
     */
    private List<Integer> propsIdList;

    /**
     * boss集合；  bossid <==> boss对象
     */
    private final Map<Integer, BossMonster> bossMonsterMap = new HashMap<>();



    public Duplicate(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * 选出当前boss
     */
    public void setMinBoss() {
        Optional<Map.Entry<Integer, BossMonster>> min =
                bossMonsterMap.entrySet().stream().min(Comparator.comparingInt(b -> b.getValue().getHp()));

        Map.Entry<Integer, BossMonster> bossMonsterEntry = min.get();
        // 进入此房间时间，
        bossMonsterEntry.getValue().setEnterRoomTime(System.currentTimeMillis());

        this.setCurrBossMonster(bossMonsterEntry.getValue());
        bossMonsterMap.remove(bossMonsterEntry.getKey());
    }

    public List<Integer> getPropsIdList() {
        List<Integer> list = new ArrayList<>();
        String[] propsId = null;
        for (DuplicateType duplicateType : DuplicateType.values()) {
            if (duplicateType.getName().equals(this.name)){
                propsId = duplicateType.getPropsId().split(",");
                break;
            }
        }

        for (int i = 0; i < DuplicateConst.PROPS_NUMBER; i++) {
            list.add(Integer.valueOf(propsId[(int)(Math.random()*(propsId.length))]));
        }

        return list;
    }


}
