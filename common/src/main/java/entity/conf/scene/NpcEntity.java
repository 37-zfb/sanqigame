package entity.conf.scene;

import lombok.*;

/**
 * @author 张丰博
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NpcEntity {
    /**
     *  NPCid
     */
    private Integer id;

    /**
     *  NPC 名称
     */
    private String name;

    /**
     * NPC 所在场景id
     */
    private Integer sceneId;

    /**
     * NPC 说的话
     */
    private String info;
}
