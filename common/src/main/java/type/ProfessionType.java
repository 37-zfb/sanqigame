package type;

/**
 * @author 张丰博
 */
public enum ProfessionType {
    /**
     *  战士
     */
    Warrior(1,"战士"),
    /**
     *  牧师
     */
    Pastor(2,"牧师"),
    /**
     *  法师
     */
    Sorceress(3,"法师"),
    /**
     *  召唤师
     */
    Summoner(4,"召唤师")
    ;

    /**
     * 装备类型
     */
    private String type;

    /**
     *  id
     */
    private Integer id;

    /**
     * @param type 类型
     */
    ProfessionType(Integer id,String type) {
        this.id = id;
        this.type = type;
    }

    /**
     * @return 类型
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @return id
     */
    public Integer getId(){return id;}

}
