package model.duplicate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BossMonster {

    /**
     *  boss id
     */
    private Integer id;

    /**
     *  副本id
     */
    private Integer duplicateId;

    /**
     *  boss 名称
     */
    private String bossName;

    /**
     *  boss 血量
     */
    private Integer hp;

    /**
     * 基础伤害
     */
    private Integer baseDamage;

    /**
     *  boss技能集合    技能id <==> 技能对象
     */
    private final Map<Integer,BossSkill> bossSkillMap = new HashMap<>();

}
