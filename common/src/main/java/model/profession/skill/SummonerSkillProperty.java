package model.profession.skill;

import lombok.Getter;
import lombok.Setter;
import type.ProfessionType;

/**
 * @author ZFB
 * 召唤师技能属性
 */
@Setter
@Getter
public class SummonerSkillProperty extends  AbstractSkillProperty {

    /**
     *  存在时间
     */
    private Integer effectTime;
    /**
     *  宠物伤害
     */
    private Integer petDamage;

    public SummonerSkillProperty(){}

    @Override
    public ProfessionType getProfessionType() {
        return ProfessionType.Summoner;
    }

    public SummonerSkillProperty(Integer id, Integer skillId, Integer effectTime, Integer petDamage) {
        super(id, skillId);
        this.effectTime = effectTime;
        this.petDamage = petDamage;
    }
}
