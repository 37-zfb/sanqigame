package client.model.server.profession.skill;

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

    public abstract ProfessionType getProfessionType();

}
