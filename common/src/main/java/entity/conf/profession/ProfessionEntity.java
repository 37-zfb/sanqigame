package entity.conf.profession;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionEntity {

    /**
     *  角色id
     */
    private Integer id;

    /**
     *  角色类型
     */
    private String profession;

    /**
     * 角色基础伤害
     */
    private Integer baseDamage;

    /**
     * 角色基础防御
     */
    private Integer baseDefense;

}
