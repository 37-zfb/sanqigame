package type;

/**
 * @author 张丰博
 */
public enum DuplicateType {
    /**
     *  领主之塔
     */
    LordTower("领主之塔",80000,"20,21,22,23,1,2,3,4,5,6,7,8,9,19"),
    /**
     *  牛头怪
     */
    Minotaur("牛头怪",30000,"20,21,22,23,10,11,12,13,14,15,16,17,18")
    ;

    private String name;

    private Integer money;

    private String propsId;

    DuplicateType(String name,Integer money,String propsId){
        this.name = name;
        this.money = money;
        this.propsId = propsId;
    }

    public String getName() {
        return name;
    }

    public Integer getMoney() {
        return money;
    }

    public String getPropsId() {
        return propsId;
    }
}
