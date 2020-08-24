package server.model.scene;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 张丰博
 * 场景基础类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scene {

    private Integer id;

    private String name;

    /**
     *  NPCid  NPC
     */
    private final Map<Integer,Npc> npcMap = new HashMap<>();
    /**
     *  怪id  怪
     */
    private final Map<Integer,Monster> monsterMap = new HashMap<>();


}
