package client.model.server.profession.skill;

import lombok.Getter;
import lombok.Setter;
import type.ProfessionType;

/**
 * @author ZFB
 *  法师技能属性
 */
@Setter
@Getter
public class SorceressSkillProperty extends AbstractSkillProperty {
    /**
     *  伤害值
     */
    private Integer damageValue;
    /**
     *  伤害百分比
     *
     */
    private float damagePercent;

    public SorceressSkillProperty(){}

    @Override
    public ProfessionType getProfessionType() {
        return ProfessionType.Sorceress;
    }


    public SorceressSkillProperty(Integer id, Integer skillId, Integer damageValue, float damagePercent) {
        super(id, skillId);
        this.damageValue = damageValue;
        this.damagePercent = damagePercent;
    }
}
