package entity.conf.profession;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 张丰博
 *法师技能
 */
@Setter
@Getter
public class SorceressSkillPropertyEntity {
    /**
     * id
     */
    private Integer id;
    /**
     *  技能id
     */
    private Integer skillId;
    /**
     *  伤害值
     */
    private Integer damageValue;
    /**
     *  伤害百分比
     *
     */
    private float damagePercent;

}
