package server.timer;

import constant.ProfessionConst;
import lombok.extern.slf4j.Slf4j;
import server.model.profession.skill.PastorSkillProperty;
import msg.GameMsg;
import server.model.PlayTeam;
import server.model.User;
import server.model.UserManager;
import util.CustomizeThreadFactory;

import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 */
@Slf4j
public class PastorSkillTimer {

    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("吟唱线程")
    );

    private static final PastorSkillTimer PASTOR_SKILL_TIMER = new PastorSkillTimer();

    public static PastorSkillTimer getInstance() {
        return PASTOR_SKILL_TIMER;
    }


    /**
     * 吟唱
     *
     * @param user
     */
    public RunnableScheduledFuture<?> userChant(User user, PastorSkillProperty skillProperty) {
        if (user == null || skillProperty == null) {
            return null;
        }

        RunnableScheduledFuture<?> scheduledFuture =
                (RunnableScheduledFuture<?>) scheduledThreadPool
                        .schedule(() -> {
                            PlayTeam playTeam = user.getPlayTeam();

                            if (playTeam != null) {
                                // 给全部队友加血；
                                Integer[] team_member = playTeam.getTEAM_MEMBER();
                                for (Integer id : team_member) {
                                    if (id == null) {
                                        continue;
                                    }

                                    User userById = UserManager.getUserById(id);
                                    if (userById == null) {
                                        continue;
                                    }

                                    addState(userById, skillProperty);

                                    GameMsg.PastorSkillResult pastorSkillResult = GameMsg.PastorSkillResult.newBuilder()
                                            .setHp(skillProperty.getRecoverHp())
                                            .setMp(skillProperty.getRecoverMp())
                                            .build();
                                    userById.getCtx().writeAndFlush(pastorSkillResult);
                                }
                            } else {
                                // 给自己加血
                                addState(user, skillProperty);
                                GameMsg.PastorSkillResult pastorSkillResult = GameMsg.PastorSkillResult.newBuilder()
                                        .setHp(skillProperty.getRecoverHp())
                                        .setMp(skillProperty.getRecoverMp())
                                        .build();
                                user.getCtx().writeAndFlush(pastorSkillResult);
                            }
                            user.setIsPrepare(null);

                        }, (long) (skillProperty.getPrepareTime() * 1000), TimeUnit.MILLISECONDS);
        return scheduledFuture;
    }

    private void addState(User userById, PastorSkillProperty skillProperty) {
        if (userById == null || skillProperty == null) {
            return;
        }

        synchronized (userById.getHpMonitor()) {
            userById.calCurrHp();
            log.info("玩家:{}, 当前血量:{} +{}", userById.getUserName(), userById.getCurrHp(), skillProperty.getRecoverHp());
            if ((userById.getCurrHp() + skillProperty.getRecoverHp()) >= ProfessionConst.HP) {
                // hp满了
                userById.setCurrHp(ProfessionConst.HP);
                userById.getUserResumeState().setEndTimeHp(0L);
            } else {
                // hp没满
                userById.setCurrHp(userById.getCurrHp() + skillProperty.getRecoverHp());
            }
        }

        synchronized (userById.getMpMonitor()) {
            // 计算当前mp
            userById.calCurrMp();
            int mp = userById.getCurrMp() + skillProperty.getRecoverMp();
            if (mp >= ProfessionConst.MP) {
                userById.setCurrMp(ProfessionConst.MP);
            } else {
                // 加mp
                userById.setCurrMp(mp);
            }
            log.info("玩家:{},+{} 当前MP:{} ", userById.getUserName(), skillProperty.getRecoverMp(), userById.getCurrMp());
            // 设置终止时间
            userById.resumeMpTime();
        }

    }


}
