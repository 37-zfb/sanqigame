package client.model.server.duplicate;

import client.model.User;
import client.model.server.profession.SummonMonster;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

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
     * 普通攻击
     */
    private int ordinaryAttack = 0;

    /**
     * 记录要攻击的用户的id
     */
    private final AtomicReference<ForceAttackUser> attackUserAtomicReference = new AtomicReference<>();

    /**
     * 团队成员攻击boss的血量记录  用户id <==> 血量
     */
    private final Map<Integer, Integer> userIdMap = new HashMap<>();
    /**
     * 召唤兽攻击boss的血量记录   召唤兽<==>血量
     */
    private final Map<SummonMonster, Integer> summonMonsterMap = new HashMap<>();


    /**
     * 选择用户监视器
     */
    private final Object ATTACK_BOSS_MONITOR = new Object();


    /**
     * 定时器
     */
    private ScheduledFuture<?> ScheduledFuture;
    /**
     * 减血 监视器
     */
    private final Object SUB_MONITOR = new Object();

    private final Object CHOOSE_USER_MONITOR = new Object();
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

    public int calUserSubHp(Integer defense, Integer weakenDefense) {
        int subHp = (int) ((Math.random() * this.getBaseDamage()) + 500) - (int) ((Math.random() * (defense - weakenDefense)) + 100);
        return subHp;
    }








}







