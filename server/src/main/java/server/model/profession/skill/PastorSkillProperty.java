package server.model.profession.skill;

import lombok.Getter;
import lombok.Setter;
import type.ProfessionType;

/**
 * @author ZFB
 * 牧师技能属性
 */
@Setter
@Getter
public class PastorSkillProperty extends AbstractSkillProperty {

    /**
     *  恢复mp值
     */
    private Integer recoverMp;
    /**
     *  恢复HP值
     */
    private Integer recoverHp;
    /**
     * 恢复MP值百分比
     */
    private float percentMp;
    /**
     *  恢复HP百分比
     */
    private float percentHp;
    /**
     *  准备时间
     */
    private float prepareTime;



    public PastorSkillProperty(){}

    @Override
    public ProfessionType getProfessionType() {
        return ProfessionType.Pastor;
    }

    public PastorSkillProperty(Integer id, Integer skillId, Integer recoverMp, Integer recoverHp, float percentMp,float percentHp,float prepareTime) {
        super(id, skillId);
        this.recoverMp = recoverMp;
        this.recoverHp = recoverHp;
        this.percentMp = percentMp;
        this.percentHp = percentHp;
        this.prepareTime = prepareTime;
    }


}
