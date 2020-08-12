package type;

/**
 * @author 张丰博
 */
public enum BossMonsterType {

    /**
     *  牛头怪
     */
    Minotaur(1,"牛头怪"),
    /**
     *  哥布林
     */
    Goblin(2,"哥布林"),
    /**
     *  猫妖
     */
    CatDemon(3,"猫妖"),
    /**
     *  机械牛
     */
    MechanicalCow(4,"机械牛")
    ;

    private Integer id;

    private String name;

    BossMonsterType(Integer id,String name){
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
