package entity.conf.duplicate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 *
 * boss 技能
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BossSkillEntity {

    /**
     *  boss技能 id
     */
    private Integer id;
    /**
     *  boss id
     */
    private Integer bossId;

    /**
     *  技能名称
     */
    private String name;

    /**
     *  技能伤害
     */
    private Integer skillDamage;

    /**
     *  对技能的描述
     */
    private String info;

}
