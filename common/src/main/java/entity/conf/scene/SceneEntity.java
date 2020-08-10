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
public class SceneEntity {
    /**
     *  id
     */
    private Integer id;

    /**
     *  场景名称
     */
    private String name;
}
