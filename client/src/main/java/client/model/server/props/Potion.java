package client.model.server.props;

import constant.ProfessionConst;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import type.PropsType;

import java.util.concurrent.ScheduledFuture;

/**
 * @author 张丰博
 */
@Setter
@Getter
@AllArgsConstructor
@ToString
public class Potion extends AbstractPropsProperty {


    /**
     * cd时间
     */
    private float cdTime;

    /**
     * 信息描述
     */
    private String info;

    /**
     * 恢复值
     */
    private Integer resumeFigure;

    /**
     * 恢复百分比
     */
    private float percent;

    /**
     * 上次cd时间
     */
    private long lastTimeSkillTime;

    /**
     * 拥有的数量
     */
    private int number;


    /**
     *  药效开始时间
     */
    private long usedStartTime;
    /**
     * 药效终止时间； 利用此字段计算当前状态
     */
    private long usedEndTime;

    /**
     * 记录恢复次数，恢复4次
     */
    private int recordResumeNumber;

    /**
     * 定时任务
     */
    private ScheduledFuture<?> task;


    public Potion() {
    }

    @Override
    public PropsType getType() {
        return PropsType.Potion;
    }

    @Override
    public PropsType isLimit() {
        return PropsType.Limit;
    }


    public Potion(Long id, Integer propsId, float cdTime, String info, Integer resumeFigure, float percent) {
        super(id, propsId);
        this.cdTime = cdTime;
        this.info = info;
        this.resumeFigure = resumeFigure;
        this.percent = percent;
    }

    public Potion(Long id, Integer propsId, float cdTime, String info, Integer resumeFigure, float percent, int number) {
        super(id, propsId);
        this.cdTime = cdTime;
        this.info = info;
        this.resumeFigure = resumeFigure;
        this.percent = percent;
        this.number = number;
    }


    /**
     * 药的状态
     *
     * @return 技能是否在冷却
     */
    public boolean isCd() {
        long currTime = System.currentTimeMillis();
        if ((currTime - this.lastTimeSkillTime) >= this.cdTime * 1000) {
            this.lastTimeSkillTime = currTime;
            return false;
        }
        return true;
    }

    public Integer calImmediatelyMpPotion() {
        return (int) (ProfessionConst.MP * this.getPercent() + this.getResumeFigure());
    }

    public Integer calImmediatelyHpPotion(){
        return (int) (ProfessionConst.HP * this.getPercent() + this.getResumeFigure());
    }



}
