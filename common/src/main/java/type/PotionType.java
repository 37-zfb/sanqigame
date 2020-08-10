package type;


/**
 * @author 张丰博
 */
public enum PotionType {

    MP("MP"),
    HP("HP"),
    IMMEDIATELY("立即"),
    SLOW("缓慢")
    ;
    /**
     *  药剂类型
     */
    private String type;

    private PotionType(String type) {
        this.type = type;
    }

    public String getType(){
        return type;
    }

}
