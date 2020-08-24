package client.model;

import client.model.server.props.Props;
import client.model.server.scene.Scene;
import client.scene.GameData;
import lombok.Data;


import java.util.HashMap;
import java.util.Map;

/**
 * @author 张丰博
 * <p>
 * 用户当前场景数据
 */
@Data
public class SceneData {

    /**
     * 场景id ==> 场景对象
     */
    private final Map<Integer, Scene> sceneMap = new HashMap<>();


    /**
     * id  道具
     */
    private  Map<Integer, Props> propsMap = new HashMap<>();


    private static final SceneData SCENE_DATA = new SceneData();

    private SceneData() {
    }

    public static SceneData getInstance() {
        return SCENE_DATA;
    }

    public void init() {
        for (Map.Entry<Integer, Scene> sceneEntry : GameData.getInstance().getSceneMap().entrySet()) {
            sceneMap.put(sceneEntry.getKey(), new Scene(sceneEntry.getValue().getId(), sceneEntry.getValue().getName()));
        }
        propsMap = GameData.getInstance().getPropsMap();
    }
}
