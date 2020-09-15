package client.timer;

import client.model.Role;
import client.model.server.props.Potion;
import constant.PotionConst;
import constant.ProfessionConst;
import lombok.extern.slf4j.Slf4j;
import util.CustomizeThreadFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 * 状态恢复定时器
 */
@Slf4j
public class ResumeStateTimer {
    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("恢复状态")
    );

    private static final ResumeStateTimer RESUME_STATE_TIMER = new ResumeStateTimer();

    private ResumeStateTimer() {
    }

    public static ResumeStateTimer getInstance() {
        return RESUME_STATE_TIMER;
    }

    /**
     * 自动回复mp
     */
    public void resumeStateAutomatic(Role role ) {
        ScheduledFuture<?> scheduledFuture = scheduledThreadPool.scheduleAtFixedRate(() -> {
            synchronized (role.getMpMonitor()) {
                if (role.getCurrMp() == ProfessionConst.MP){
                    role.getUserResumeState().setEndTimeMp(0L);
                    role.getMpTask().cancel(true);
                    role.setMpTask(null);
                    return;
                }

                if (role.getCurrMp() < ProfessionConst.MP) {
                    // 增加mp 此时需要加锁；
                    log.info("当前mp: {} ,+1", role.getCurrMp());
                    if ((role.getCurrMp() + 1) >= ProfessionConst.MP) {
                        role.setCurrMp(ProfessionConst.MP);
                    } else {
                        role.setCurrMp(role.getCurrMp() + ProfessionConst.AUTO_RESUME_MP_VALUE);
                    }


                } else {
                    // 此时，满状态
                    log.info("当前mp: {} ,取消定时器;", role.getCurrMp());
                    role.getUserResumeState().setEndTimeMp(0L);
                    role.getMpTask().cancel(true);
                    role.setMpTask(null);
                }
            }
        }, 1, 1, TimeUnit.SECONDS);

        role.setMpTask(scheduledFuture);
    }


    /**
     *  药剂恢复mp
     * @param role 玩家对象
     */
    public void resumeStatePotionMp(Role role, Potion potion) {
        ScheduledFuture<?> scheduledFuture = scheduledThreadPool.scheduleAtFixedRate(() -> {
            synchronized (role.getMpMonitor()) {
                if (potion.getRecordResumeNumber() < 4){
                    if (role.getCurrMp() < ProfessionConst.MP) {
                        // 增加mp 此时需要加锁；
                        log.info("当前mp: {} ,+100", role.getCurrMp());
                        if ((role.getCurrMp() + PotionConst.SLOW_MP_POTION_VALUE) >= ProfessionConst.MP) {
                            role.setCurrMp(ProfessionConst.MP);
                        } else {
                            role.setCurrMp(role.getCurrMp() + PotionConst.SLOW_MP_POTION_VALUE);
                        }

                    }
                    // 加1，
                    potion.setRecordResumeNumber(potion.getRecordResumeNumber()+1);
                }else {
                    // 此时，满状态
                    log.info("当前mp: {} ,取消药剂定时器;", role.getCurrMp());
                    potion.getTask().cancel(true);
                    potion.setRecordResumeNumber(0);
                    potion.setUsedEndTime(0L);
                }
            }
        }, 1, 1, TimeUnit.SECONDS);

        potion.setTask(scheduledFuture);
    }


    /**
     *  药剂恢复mp
     * @param role 玩家对象
     */
    public void resumeStatePotionHp(Role role, Potion potion) {
        ScheduledFuture<?> scheduledFuture = scheduledThreadPool.scheduleAtFixedRate(() -> {
            synchronized (role.getHpMonitor()) {
                if (potion.getRecordResumeNumber() < 4){
                    if (role.getCurrHp() < ProfessionConst.HP) {
                        // 增加mp 此时需要加锁；
                        log.info("当前hp: {} ,+100", role.getCurrHp());
                        if ((role.getCurrHp() + PotionConst.SLOW_HP_POTION_VALUE) >= ProfessionConst.HP) {
                            role.setCurrHp(ProfessionConst.HP);
                        } else {
                            role.setCurrHp(role.getCurrHp() + PotionConst.SLOW_HP_POTION_VALUE);
                        }

                    }
                    // 加1，
                    potion.setRecordResumeNumber(potion.getRecordResumeNumber()+1);
                }else {
                    // 此时，满状态
                    log.info("当前mp: {} ,取消药剂定时器;", role.getCurrHp());
                    potion.getTask().cancel(true);
                    potion.setRecordResumeNumber(0);
                    potion.setUsedEndTime(0L);
                }
            }
        }, 1, 1, TimeUnit.SECONDS);

        potion.setTask(scheduledFuture);
    }


}
