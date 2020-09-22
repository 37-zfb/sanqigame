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
public class PastorSkillPropertyEntity {

    /**
     *  id
     */
    private Integer id;
    /**
     *  技能id
     */
    private Integer skillId;
    /**
     *  恢复mp值
     */
    private Integer recoverMp;
    /**
     *  恢复HP值
     */
    private Integer recoverHp;
    /**
     * 恢复MP值
     */
    private float percentMp;
    /**
     *  恢复HP百分比
     */
    private float percentHp;
    /**
     *  恢复MP百分比
     */
    private float prepareTime;


    /**
     * 护盾值
     */
    private Integer shieldValue;

}
