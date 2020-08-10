package entity.conf.profession;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarriorSkillPropertyEntity {

    /**
     *  id
     */
    private Integer id;

    /**
     *  技能id
     */
    private Integer skillId;

    /**
     *  影响时间
     */
    private float effectTime;

    /**
     *  伤害
     */
    private Integer damage;

    /**
     *  百分比伤害
     */
    private Integer percentDamage;

}
