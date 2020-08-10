package type;

/**
 * @author 张丰博
 *
 *  道具类型枚举
 */

public enum PropsType {
    /**
     *  装备
     */
    Equipment("装备"),
    /**
     *  药剂
     */
    Potion("药剂")
    ;

    /**
     *  类型
     */
    private String type;

    PropsType(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
