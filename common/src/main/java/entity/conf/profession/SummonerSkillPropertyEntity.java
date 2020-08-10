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
public class SummonerSkillPropertyEntity {
    /**
     *  id
     */
    private Integer id;

    /**
     *  技能id
     */
    private Integer skillId;
    /**
     *  存在时间
     */
    private Integer effectTime;
    /**
     *  宠物伤害
     */
    private Integer petDamage;

}
