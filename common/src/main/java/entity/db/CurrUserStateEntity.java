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

    /**
     * id
     */
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 当前血量
     */
    private Integer currHp;

    /**
     * 当前蓝量
     */
    private Integer currMp;

    /**
     * 当前场景id
     */
    private Integer currSceneId;

    /**
     * 基础伤害
     */
    private Integer baseDamage;

    /**
     * 基础防御
     */
    private Integer baseDefense;

    /**
     * 金币
     */
    private Integer money;

    /**
     * 公会id
     */
    private Integer guildId;

    /**
     * 等级
     */
    private Integer lv ;

    /**
     * 经验
     */
    private long experience;
}
