package type;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 张丰博
 * 场景类型
 */
public enum SceneType {

    /**
     * 城镇类型
     */
    StartPlace(1,"城镇"),
    Village(2,"城镇"),

    /**
     * 野外
     */
    Forest(3,"野外"),
    Castle(4,"野外"),

    ;

    /**
     * 场景id
     */
    private Integer sceneId;
    /**
     * 场景类型
     */
    private String type;

    private SceneType(Integer sceneId, String type) {
        this.sceneId = sceneId;
        this.type = type;
    }

    /**
     * @param type
     * @return
     */
    public static List<Integer> getSceneIdByType(String type){
        ArrayList<Integer> list = new ArrayList<>();
        for (SceneType sceneType : SceneType.values()) {
            if (sceneType.type.equals(type)){
                list.add(sceneType.sceneId);
            }
        }
        return list;
    }

}
