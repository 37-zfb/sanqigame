package type.skill;

/**
 * @author 张丰博
 *  召唤师
 */
public enum SummonerSkillType {

    /**
     *  嘲讽
     */
    SUMMON(1, "summon");

    /**
     *  战士技能id
     */
    private Integer id;

    /**
     *  技能名称
     */
    private String name;

    SummonerSkillType(Integer id, String name) {
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
