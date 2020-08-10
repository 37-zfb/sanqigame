package model.scene;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Npc {
    private Integer id;

    private String name;

    private Integer sceneId;

    private String info;
}
