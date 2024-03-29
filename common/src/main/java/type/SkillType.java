package type;

/**
 * @author 张丰博
 */
public enum SkillType {

    /**
     * 嘲讽
     */
    RIDICULE(1, "server.cmdhandler.skill.warrior.RidiculeHandler"),

    /**
     * 治疗
     */
    TREATMENT(2, "server.cmdhandler.skill.pastor.TreatmentHandler"),

    /**
     * 全体攻击
     */
    GROUP_ATTACK(3, "server.cmdhandler.skill.sorceress.GroupAttackHandler"),

    /**
     * 召唤术
     */
    SUMMON(4, "server.cmdhandler.skill.summoner.SummonHandler"),

    /**
     * 护盾
     */
    SHIELD(5, "server.cmdhandler.skill.pastor.ShieldHandler"),

    /**
     * 放毒
     */
    POISON(6, "server.cmdhandler.skill.warrior.PoisonHandler"),
    ;
    private Integer skillId;

    private String clazzName;


    SkillType(Integer skillId, String clazzName) {
        this.skillId = skillId;
        this.clazzName = clazzName;
    }


    public Integer getSkillId() {
        return skillId;
    }

    public String getClazzName() {
        return clazzName;
    }

    public static String getClazzNameById(Integer skillId) {
        if (skillId == null) {
            return null;
        }

        for (SkillType skillType : SkillType.values()) {
            if (!skillType.getSkillId().equals(skillId)) {
                continue;
            }

            return skillType.getClazzName();
        }
        return null;
    }


}
