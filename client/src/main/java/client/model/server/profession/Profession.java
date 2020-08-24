package client.model.server.profession;


import lombok.Data;
import type.ProfessionType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 张丰博
 */
@Data
public class Profession {
    /**
     *  角色id
     */
    private Integer id;

    /**
     *  角色类型
     */
    private ProfessionType professionType;

    /**
     * 角色基础伤害
     */
    private Integer baseDamage;

    /**
     * 角色基础防御
     */
    private Integer baseDefense;

    /**
     *  该职业应该拥有的技能
     */
    private final Map<Integer , Skill> skillMap = new HashMap<>();

    public Profession(){}

    public Profession(Integer id, ProfessionType professionType, Integer baseDamage, Integer baseDefense){
        this.id = id;
        this.professionType = professionType;
        this.baseDamage = baseDamage;
        this.baseDefense = baseDefense;
    }

}
