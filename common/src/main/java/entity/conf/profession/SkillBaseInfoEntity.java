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
public class SkillBaseInfoEntity {

    /**
     *  id
     */
    private Integer id;

    /**
     *  职业id
     */
    private Integer professionId;

    /**
     *  姓名
     */
    private String name;

    /**
     *  冷却时间
     */
    private float cdTime;

    /**
     * 技能介绍
     */
    private String introduce;

    /**
     * 耗蓝量
     */
    private Integer consumeMp;

}
