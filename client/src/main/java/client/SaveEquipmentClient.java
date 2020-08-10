package client;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 张丰博
 * 保存，爆出来的装备
 */
public class SaveEquipmentClient {

    /**
     * 保存爆的装备    monsterId <==> 装备Id集合
     */
    private static final Map<Integer, Integer> SAVE_EQUIPMENT = new HashMap<>();

    private static final Map<Integer, Integer> SAVE_PROPS = new HashMap<>();

    public static  void saveProps(Integer monsterId, Integer propsId) {
        if (monsterId == null || propsId == null) {
            return;
        }
        SAVE_PROPS.put(monsterId, propsId);
    }

    public static  Integer getPropsIdByMonsterId(Integer monsterId) {
        if (monsterId == null) {
            return null;
        }
        Integer propsId = SAVE_PROPS.get(monsterId);
        SAVE_PROPS.remove(monsterId);
        return propsId;
    }


    public static  void saveEqu(Integer monsterId, Integer equipmentId) {
        if (monsterId == null || equipmentId == null) {
            return;
        }
        SAVE_EQUIPMENT.put(monsterId, equipmentId);
    }

    public static  Integer getEquIdByMonsterId(Integer monsterId) {
        if (monsterId == null) {
            return null;
        }
        Integer equId = SAVE_EQUIPMENT.get(monsterId);
        SAVE_EQUIPMENT.remove(monsterId);
        return equId;
    }

}
