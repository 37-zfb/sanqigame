package entity.conf.duplicate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 *
 * boss
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BossEntity {

    /**
     *  boss id
     */
    private Integer id;

    /**
     *  副本id
     */
    private Integer duplicateId;

    /**
     *  boss 名称
     */
    private String bossName;

    /**
     *  boss 血量
     */
    private Integer hp;

    /**
     * 基础伤害
     */
    private Integer baseDamage;
}
