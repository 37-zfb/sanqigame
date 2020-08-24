package constant.info;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author 张丰博
 */
@Getter
@ToString
public final class EquipmentConst {

    /**
     * 最大耐久度
     */
    public  int MAX_DURABILITY;

    /**
     *  穿戴状态
     */
    public  int WARE;

    /**
     * 未穿戴状态
     */
    public int NO_WARE;

    private static EquipmentConst equipmentConst = null;

    private EquipmentConst() {
    }

    public static EquipmentConst getEquipmentConst(){
        return equipmentConst;
    }

    private void init(EquipmentConst equipmentConst){
        EquipmentConst.equipmentConst = equipmentConst;
    }

}
