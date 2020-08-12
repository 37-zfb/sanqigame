package model.duplicate;

import com.alibaba.druid.support.ibatis.SqlMapClientImplWrapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 张丰博
 */
@Slf4j
@Data
@NoArgsConstructor
public class BossMonster {

    /**
     * boss id
     */
    private Integer id;

    /**
     * 副本id
     */
    private Integer duplicateId;

    /**
     * boss 名称
     */
    private String bossName;

    /**
     * boss 血量
     */
    private Integer hp;

    /**
     * 基础伤害
     */
    private Integer baseDamage;

    /**
     * 进入此房间的时间
     */
    private long enterRoomTime;

    /**
     *  普通攻击
     */
    private int ordinaryAttack = 0;

    /**
     *  记录要攻击的用户的id
     */
    private Integer userId;
    /**
     * 团队成员攻击boss的血量记录  用户id <==> 血量
     */
    private final Map<Integer,Integer> userIdMap = new HashMap<>();
    /**
     * 选择用户监视器
     */
    private final Object chooseUserMonitor = new Object();


    /**
     * 定时器
     */
    private ScheduledFuture<?> ScheduledFuture;
    /**
     * 减血 监视器
     */
    private final Object subHpMonitor = new Object();

    /**
     * boss技能集合    技能id <==> 技能对象
     */
    private final Map<Integer, BossSkill> bossSkillMap = new HashMap<>();

    public BossMonster(Integer id, Integer duplicateId, String bossName, Integer hp, Integer baseDamage) {
        this.id = id;
        this.duplicateId = duplicateId;
        this.bossName = bossName;
        this.hp = hp;
        this.baseDamage = baseDamage;
    }

    public int calUserSubHp(Integer defense,Integer weakenDefense) {
        int subHp = (int) ((Math.random() * this.getBaseDamage()) + 500) - (int) ((Math.random() * (defense-weakenDefense)) + 100);
        return subHp;
    }

}
