package model.duplicate;

import com.sun.org.apache.xpath.internal.axes.ChildTestIterator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
public class BossSkill {

    /**
     *  boss技能 id
     */
    private Integer id;
    /**
     *  boss id
     */
    private Integer bossId;

    /**
     *  技能名称
     */
    private String name;

    /**
     *  伤害
     */
    private Integer damage;

    /**
     *  对技能的描述
     */
    private String info;


    public BossSkill(Integer id,Integer bossId,String name,Integer damage,String info){
        this.id = id;
        this.bossId = bossId;
        this.name = name;
        this.damage = damage;
        this.info = info;
    }

}
