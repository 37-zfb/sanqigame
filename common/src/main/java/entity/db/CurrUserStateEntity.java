package entity.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrUserStateEntity {

    private Integer id;

    private Integer userId;

    private Integer currHp;

    private Integer currMp;

    private Integer currSceneId;

    private Integer baseDamage;

    private Integer baseDefense;

}
