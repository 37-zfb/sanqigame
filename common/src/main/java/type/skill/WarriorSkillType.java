package type.skill;

/**
 * @author 张丰博
 */
public enum WarriorSkillType {
    /**
     *  嘲讽
     */
    RIDICULE(1, "ridicule");

    /**
     *  战士技能id
     */
    private Integer id;

    /**
     *  技能名称
     */
    private String name;

    WarriorSkillType(Integer id, String name) {
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
