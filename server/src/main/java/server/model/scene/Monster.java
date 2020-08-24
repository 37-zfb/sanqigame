package server.model.scene;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import server.model.duplicate.ForceAttackUser;
import server.model.profession.SummonMonster;
import server.model.User;
import server.model.UserManager;

import java.util.*;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author 张丰博
 */
@Slf4j
@Data
@NoArgsConstructor
public class Monster {

    private Integer id;

    /**
     * 场景id
     */
    private Integer sceneId;

    /**
     * 怪名
     */
    private String name;


    /**
     *  血量
     */
    private Integer hp;

    /**
     *  道具id字符串
     */
    private String propsId;


    /**
     * 怪减血监听对象
     */
    private final Object subHpMonitor = new Object();

    /**
     *  记录要攻击的用户的id
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
     *  选择攻击目标监视器
     *
     */
    private final Object CHOOSE_USER_MONITOR = new Object();


    /**
     *  怪攻击定时器
     */
    private  RunnableScheduledFuture runnableScheduledFuture;



    /**
     * 自动掉血定时任务
     */
    private ScheduledFuture<?> scheduledFuture;
    /**
     * 记录掉血次数
     */
    private final AtomicInteger dropHpNumber = new AtomicInteger(0);



    public Monster(Integer id, Integer sceneId, String name, Integer hp) {
        this.id = id;
        this.sceneId = sceneId;
        this.name = name;
        this.hp = hp;
    }

    public Monster(Integer id, Integer sceneId, String name, Integer hp, String propsId) {
        this.id = id;
        this.sceneId = sceneId;
        this.name = name;
        this.hp = hp;
        this.propsId = propsId;
    }

    /**
     * 定时任务
     */
//    private final Map<User, RunnableScheduledFuture> timerMap = new HashMap<>();
    public boolean isDie() {
        if (this.hp <= 0) {
            return true;
        } else {
            return false;
        }
    }





    /**
     *  添加或更新用户id进入Map
     * @param userId
     * @param subHp
     */
    public void putUserIdMap(Integer userId,Integer subHp){
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
     *  选择当前对boss上海最高的用户；
     * @return 返回user对象
     */
    public User chooseUser() {
        User user = null;
        synchronized (this.getCHOOSE_USER_MONITOR()) {
            // 选择对boss伤害最高的用户
            AtomicReference<ForceAttackUser> atomicReference = this.getAttackUserAtomicReference();
            ForceAttackUser forceAttackUser = atomicReference.get();
            if (forceAttackUser != null && System.currentTimeMillis() < forceAttackUser.getEndTime()) {
                user = UserManager.getUserById(forceAttackUser.getUserId());
                log.info("用户 {} 吸引boss {} 的攻击;", user.getUserName(), this.getName());
            } else {
                Map<Integer, Integer> userIdMap = this.getUserIdMap();
                while (userIdMap.size() > 0 && user == null) {
                    Optional<Map.Entry<Integer, Integer>> max = userIdMap.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue));
                    user = UserManager.getUserById(max.get().getKey());
                    if (user.getCurrHp() <= 0) {
                        userIdMap.remove(user.getUserId());
                        user = null;
                    }
                    log.info("用户 {} 对boss减血量 {}", user.getUserName(), max.get().getValue());
                }
                atomicReference.compareAndSet(forceAttackUser, null);

            }
        }
        return user;
    }


    /**
     * 选择攻击boss血量最高的召唤兽
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
                log.info("召唤兽,对boss减血量 {}", max.get().getValue());
            }
        }
        return summonMonster;
    }

    /**
     *  计算用户减血量
     * @return
     */
    public int calUserSubHp() {
        return new Random().nextInt(300);
    }






}
