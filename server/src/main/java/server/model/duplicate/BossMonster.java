package server.model.duplicate;

import constant.BossMonsterConst;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.apache.poi.ss.formula.functions.T;
import server.PublicMethod;
import server.cmdhandler.duplicate.BossSkillAttack;
import server.model.profession.SummonMonster;
import server.model.User;
import server.model.UserManager;

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


    /**
     *
     */
    public Integer getForceId(){
        AtomicReference<ForceAttackUser> atomicReference = this.getAttackUserAtomicReference();
        ForceAttackUser forceAttackUser = atomicReference.get();
        if (forceAttackUser != null && System.currentTimeMillis() < forceAttackUser.getEndTime()) {
            return forceAttackUser.getUserId();
        } else {
            atomicReference.compareAndSet(forceAttackUser, null);
        }
        return null;
    }


    /**
     * 选择当前对boss伤害最高的用户；
     *
     * @return 返回user对象
     */
    public User chooseUser() {
        User user = null;
        synchronized (this.getCHOOSE_USER_MONITOR()) {
            // 选择对boss伤害最高的用户
            Map<Integer, Integer> userIdMap = this.getUserIdMap();
            while (userIdMap.size() > 0 && user == null) {
                Optional<Map.Entry<Integer, Integer>> max = userIdMap.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue));
                user = UserManager.getUserById(max.get().getKey());
                if (user.getCurrHp() <= 0) {
                    userIdMap.remove(user.getUserId());
                    user = null;
                }
//                log.info("用户 {} 对boss减血量 {}", user.getUserName(), max.get().getValue());
            }
        }
        return user;
    }


    /**
     * 选择攻击boss血量最高的召唤兽
     *
     * @return 召唤兽对象，当没有召唤兽攻击boss时为null；
     */
    public SummonMonster chooseSummonMonster() {
        SummonMonster summonMonster = null;
        synchronized (this.getCHOOSE_USER_MONITOR()) {
            Map<SummonMonster, Integer> summonMonsterMap = this.getSummonMonsterMap();
            while (summonMonsterMap.size() > 0 && summonMonster == null) {
                Optional<Map.Entry<SummonMonster, Integer>> max = summonMonsterMap.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue));
                summonMonster = max.get().getKey();
                if (summonMonster.getHp() <= 0) {
                    summonMonsterMap.remove(summonMonster);
                    summonMonster = null;
                }
//                log.info("召唤兽,对boss减血量 {}", max.get().getValue());
            }
        }
        return summonMonster;
    }

    /**
     * 添加或更新用户id进入Map
     *
     * @param userId
     * @param subHp
     */
    public void putUserIdMap(Integer userId, Integer subHp) {
        if (userId == null || subHp == null){
            return;
        }
        synchronized (this.getCHOOSE_USER_MONITOR()) {
            Map<Integer, Integer> userIdMap = this.getUserIdMap();
            if (!userIdMap.containsKey(userId)) {
                userIdMap.put(userId, subHp);
            } else {
                Integer oldSubHp = userIdMap.get(userId);
                userIdMap.put(userId, oldSubHp + subHp);
            }
        }
    }

    /**
     * 添加或更新召唤兽攻击血量
     * @param summonMonster
     * @param subHp
     */
    public void putSummonMonsterMap(SummonMonster summonMonster,Integer subHp){
        if (summonMonster == null || subHp == null){
            return;
        }
        synchronized (this.getCHOOSE_USER_MONITOR()) {
            Map<SummonMonster, Integer> summonMonsterMap = this.getSummonMonsterMap();
            if (!summonMonsterMap.containsKey(summonMonster)) {
                summonMonsterMap.put(summonMonster, subHp);
            } else {
                Integer oldSubHp = summonMonsterMap.get(summonMonster);
                summonMonsterMap.put(summonMonster, oldSubHp + subHp);
            }
        }


    }




}







