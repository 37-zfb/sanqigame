package client.model.server.scene;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

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
     * 团队成员攻击boss的血量记录  用户id <==> 血量
     */
    private final Map<Integer, Integer> userIdMap = new HashMap<>();


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
     *  计算用户减血量
     * @return
     */
    public int calUserSubHp() {
        return new Random().nextInt(300);
    }






}
