package server.cmdhandler.potionhandler;

import constant.ProfessionConst;
import lombok.extern.slf4j.Slf4j;
import model.props.Potion;
import org.springframework.stereotype.Component;
import server.model.User;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public class ImmediatelyResume {


    /**
     * 立即恢复MP
     *
     * @param user   当前对象
     * @param potion 药剂对象
     */
    public void immediatelyResumeMp(User user, Potion potion) {
        synchronized (user.getMpMonitor()) {
            // 计算当前mp
            user.calCurrMp();
            int addMp = potion.calImmediatelyMpPotion();
            int mp = user.getCurrMp() + addMp;
            if (mp >= ProfessionConst.MP) {
                user.setCurrMp(ProfessionConst.MP);
            } else {

                // 加mp
                user.setCurrMp(mp);
            }
            log.info("玩家:{}, 当前血量:{} ", user.getUserName(), user.getCurrHp());
            // 设置终止时间
            user.resumeMpTime();
        }
    }


    /**
     * 立即恢复hp
     *
     * @param user   当前用户对象
     * @param potion 药剂对象
     */
    public void immediatelyResumeHp(User user, Potion potion) {
        synchronized (user.getHpMonitor()) {
            user.calCurrHp();
            int addHP = potion.calImmediatelyHpPotion();
            log.info("玩家:{}, 当前血量:{} +{}", user.getUserName(), user.getCurrHp(), addHP);
            if ((user.getCurrHp() + addHP) >= ProfessionConst.HP) {
                // hp满了
                user.setCurrHp(ProfessionConst.HP);
                user.getUserResumeState().setEndTimeHp(0L);
            } else {
                // hp没满
                user.setCurrHp(user.getCurrHp() + addHP);
            }
        }
    }


}
