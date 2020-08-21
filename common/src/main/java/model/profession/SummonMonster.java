package model.profession;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

/**
 * @author 张丰博
 */
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
        return startTime == that.startTime &&
                endTime == that.endTime &&
                weakenDefense == that.weakenDefense &&
                subHpNumber == that.subHpNumber &&
                Objects.equals(skillId, that.skillId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(hp, that.hp) &&
                Objects.equals(damage, that.damage) &&
                Objects.equals(baseDefense, that.baseDefense) &&
                Objects.equals(ctx, that.ctx) &&
                Objects.equals(subHpMonitor, that.subHpMonitor) &&
                Objects.equals(subHpTask, that.subHpTask);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillId, name, hp, damage, startTime, endTime, baseDefense, weakenDefense, ctx, subHpMonitor, subHpNumber, subHpTask);
    }
}
