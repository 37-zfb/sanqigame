package client.model.server.props;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import type.EquipmentType;
import type.PropsType;

/**
 * @author 张丰博
 */
@Setter
@Getter
@ToString
public class Equipment extends AbstractPropsProperty {

    /**
     * 耐久度
     */
    private Integer durability;

    /**
     * 伤害
     */
    private Integer damage;

    /**
     *  数量
     */
    private final Integer number = 1;

    /**
     * 装备类型
     */
    private EquipmentType equipmentType;

    public Equipment(Long id, Integer propsId, Integer durability, Integer damage, EquipmentType equipmentType) {
        super(id, propsId);
        this.durability = durability;
        this.damage = damage;
        this.equipmentType = equipmentType;
    }

    @Override
    public PropsType getType() {
        return PropsType.Equipment;
    }

    @Override
    public PropsType isLimit() {
        return PropsType.NoLimit;
    }
}
