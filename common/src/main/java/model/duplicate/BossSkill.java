package model.duplicate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BossSkill {

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
     *  伤害
     */
    private Integer damage;

    /**
     *  对技能的描述
     */
    private String info;

}
