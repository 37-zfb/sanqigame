package server.model.profession;

import constant.BossMonsterConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import server.model.duplicate.BossMonster;
import msg.GameMsg;
import server.PublicMethod;
import server.cmdhandler.duplicate.BossSkillAttack;
import server.model.User;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

/**
 * @author 张丰博
 */
@Slf4j
@Data
@NoArgsConstructor
public class SummonMonster {

    /**
     * 技能id
     */
    private Integer skillId;

    /**
     * 召唤兽名称
     */
    private String name;

    /**
     * 血量
     */
    private Integer hp;

    /**
     * 攻击力
     */
    private Integer damage;

    /**
     * 召唤兽开始时间
     */
    private long startTime;

    /**
     * 召唤兽结束时间
     */
    private long endTime;


    private Integer baseDefense = 100;

    private int weakenDefense = 0;


    private ChannelHandlerContext ctx;

    /**
     * 减血监视器
     */
    private final Object subHpMonitor = new Object();
    private int subHpNumber = 0;
    private ScheduledFuture<?> subHpTask;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SummonMonster that = (SummonMonster) o;
        return Objects.equals(skillId, that.skillId) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillId, name);
    }

    /**
     * 受到boss攻击，减血
     *
     * @param bossMonster boss对象
     * @param subHp       减血量
     */
    public void bossAttackSubHp(BossMonster bossMonster, Integer subHp, User user) {
        // 防止多线程执行时，减血超减
        if (bossMonster.getOrdinaryAttack() > BossMonsterConst.ORDINARY_ATTACK) {
            //十秒后，防御属性回归正常
            this.setWeakenDefense(0);
            bossMonster.setOrdinaryAttack(0);
            // 每五次普通攻击，一次技能攻击
            BossSkillAttack.getInstance().bossSkillAttack(user, bossMonster, this);
        }
        GameMsg.SummonMonsterSubHpResult.Builder newBuilder = GameMsg.SummonMonsterSubHpResult.newBuilder();
        synchronized (this.getSubHpMonitor()) {
            // 召唤兽减血
            if (this.getHp() <= 0 || (this.getHp() - subHp) <= 0) {
                log.info("召唤兽已死亡;");
                this.setHp(0);
                // boss打死了 召唤兽;
                newBuilder.setIsDie(true);
            } else {
                log.info("召唤兽 , 当前血量: {} , 受到伤害减血: {}", this.getHp(), subHp);
                // 普通攻击数 加一;
                bossMonster.setOrdinaryAttack(bossMonster.getOrdinaryAttack() + 1);
                // 用户减血
                this.setHp(this.getHp() - subHp);
                newBuilder.setIsDie(false)
                        .setSubHp(subHp);
            }
            GameMsg.SummonMonsterSubHpResult summonMonsterSubHpResult = newBuilder.build();
            sendMsg(summonMonsterSubHpResult);
        }
    }

    /**
     * 发送消息
     *
     * @param summonMonsterSubHpResult 消息对象
     */
    private void sendMsg(GameMsg.SummonMonsterSubHpResult summonMonsterSubHpResult) {
        PublicMethod publicMethod = PublicMethod.getInstance();
        publicMethod.sendMsg(ctx, summonMonsterSubHpResult);
    }

    /**
     * 怪攻击，减血
     *
     * @param subHp 减血量
     */
    public void monsterAttackSubHp(Integer subHp) {
        synchronized (this.subHpMonitor) {
            GameMsg.SummonMonsterSubHpResult.Builder newBuilder = GameMsg.SummonMonsterSubHpResult.newBuilder();
            // 用户减血
            if (this.getHp() <= 0 || (this.getHp() - subHp) <= 0) {
                this.setHp(0);
                User user = PublicMethod.getInstance().getUser(ctx);
                PublicMethod.getInstance().cancelMonsterAttack(user);
                // 发送死亡消息
                newBuilder.setIsDie(true);
            } else {
                this.setHp(this.getHp() - subHp);
                newBuilder.setIsDie(false)
                        .setSubHp(subHp);
            }
            GameMsg.SummonMonsterSubHpResult summonMonsterSubHpResult = newBuilder.build();
            sendMsg(summonMonsterSubHpResult);
        }
    }

    /**
     * 计算怪应该减去的血量
     *
     * @return 血量值
     */
    public int calMonsterSubHp() {
        return (int) ((Math.random() * this.getDamage()) + 300);
    }
}
