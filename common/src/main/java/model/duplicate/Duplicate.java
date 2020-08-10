package model.duplicate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 张丰博
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Duplicate {

    /**
     * 副本id
     */
    private Integer id;

    /**
     * 副本名称
     */
    private String name;


    /**
     * boss集合；  bossid <==> boss对象
     */
    private final Map<Integer, BossMonster> bossMonsterMap = new HashMap<>();

}
