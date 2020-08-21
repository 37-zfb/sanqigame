package type.skill;

/**
 * @author 张丰博
 * 法师
 */
public enum  SorceressSkillType {
    /**
     *  治疗
     */
    ALL_ATTACK(1, "allAttack");

    /**
     *  法师技能id
     */
    private Integer id;

    /**
     *  技能名称
     */
    private String name;

    SorceressSkillType(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
