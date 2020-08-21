package type.skill;

/**
 * @author 张丰博
 * 牧师
 */
public enum PastorSkillType {
    /**
     *  治疗
     */
    THERAPY(1, "therapy");

    /**
     *  牧师技能id
     */
    private Integer id;

    /**
     *  技能名称
     */
    private String name;

    PastorSkillType(Integer id, String name) {
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
