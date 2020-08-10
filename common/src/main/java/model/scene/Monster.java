package model.scene;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;

/**
 * @author 张丰博
 */
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
    private final Object subHpMontor = new Object();


    private final Map<Integer, RunnableScheduledFuture> timerMap = new HashMap<>();

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
     * 爆装备
     *
     * @return 装备id
     */
    public Integer createProps() {

        return 0;
    }


    /**
     * 开始掉血
     *
     * @param ctx
     */
    public void startDropHp(ChannelHandlerContext ctx) {

//        if (this.scheduledFuture == null){
//            MonsterDropHpAuto.getInstance().monsterDropHpAuto(this,ctx);
//        }

    }

}
