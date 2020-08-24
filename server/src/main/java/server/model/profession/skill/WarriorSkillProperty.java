package server.model.profession.skill;

import lombok.Getter;
import lombok.Setter;
import type.ProfessionType;

/**
 * @author ZFB
 * 战士技能属性
 */
@Setter
@Getter
public class WarriorSkillProperty extends AbstractSkillProperty {
    /**
     * 影响时间
     */
    private float effectTime;

    /**
     * 伤害
     */
    private Integer damage;

    /**
     * 百分比伤害
     */
    private Integer percentDamage;

    public WarriorSkillProperty() {
    }

    @Override
    public ProfessionType getProfessionType() {
        return ProfessionType.Warrior;
    }

    public WarriorSkillProperty(Integer id, Integer skillId, float effectTime, Integer damage, Integer percentDamage) {
        super(id, skillId);
        this.effectTime = effectTime;
        this.damage = damage;
        this.percentDamage = percentDamage;
    }



}
