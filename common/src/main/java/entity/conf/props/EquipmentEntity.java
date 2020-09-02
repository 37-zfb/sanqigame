package entity.conf.props;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EquipmentEntity {
    /**
     *  装备id
     */
    private int id;

    /**
     *  对应的道具id
     */
    private Integer propsId;


    /**
     *  耐久度
     */
    private Integer durability ;

    /**
     *  伤害
     */
    private Integer damage;

    /**
     *  装备类型
     */
    private String type;
}
