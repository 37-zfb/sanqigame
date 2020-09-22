package server.model.profession;

import lombok.AllArgsConstructor;
import lombok.Data;
import server.model.profession.skill.AbstractSkillProperty;

/**
 * @author 张丰博
 */
@AllArgsConstructor
@Data
public class Skill {

    /**
     * id
     */
    private Integer id;

    /**
     * 职业id
     */
    private Integer professionId;

    /**
     * 名称
     */
    private String name;

    /**
     * 冷却时间
     */
    private float cdTime;

    /**
     * 技能介绍
     */
    private String introduce;

    /**
     * 耗蓝量
     */
    private Integer consumeMp;


    /**
     *  上次使用该技能时间
     */
    private long lastUseTime;

    /**
     * 技能其他属性
     */
    private AbstractSkillProperty skillProperty;





    public Skill() {
    }

    public Skill(Integer id, Integer professionId, String name, float cdTime, String introduce,Integer consumeMp) {
        this.id = id;
        this.professionId = professionId;
        this.name = name;
        this.cdTime = cdTime;
        this.introduce = introduce;
        this.consumeMp = consumeMp;
    }
    public Skill(Integer id, Integer professionId, String name, float cdTime, String introduce,Integer consumeMp,AbstractSkillProperty skillProperty) {
        this.id = id;
        this.professionId = professionId;
        this.name = name;
        this.cdTime = cdTime;
        this.introduce = introduce;
        this.consumeMp = consumeMp;
        this.skillProperty = skillProperty;
    }

    public boolean isCd(){
        long  timeDifference = System.currentTimeMillis()-lastUseTime;
        if (timeDifference >= cdTime){
            return false;
        }
        return true;
    }

}
