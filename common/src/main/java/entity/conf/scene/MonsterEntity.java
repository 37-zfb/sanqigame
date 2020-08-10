package entity.conf.scene;

import lombok.*;

/**
 * @author 张丰博
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MonsterEntity {
    /**
     *  怪id
     */
    private Integer id;

    /**
     *  场景id
     */
    private Integer sceneId;

    /**
     *  怪名
     */
    private String name;

    /**
     *  hp
     */
    private Integer hp;

    /**
     *  道具id
     */
    private String propsId;


}
