package server.model.profession.skill;

import lombok.Getter;
import lombok.Setter;
import type.ProfessionType;

/**
 * @author 张丰博
 */
@Setter
@Getter
public abstract class AbstractSkillProperty {
    private Integer id;

    private Integer skillId;

    public AbstractSkillProperty(){}

    public AbstractSkillProperty(Integer id,Integer skillId){
        this.id = id;
        this.skillId = skillId;
    }

    /**
     * 获取职业 类型
     * @return
     */
    public abstract ProfessionType getProfessionType();

}
