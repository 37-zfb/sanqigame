package server.cmdhandler.potionhandler;

import constant.PotionConst;
import model.props.Potion;
import org.springframework.stereotype.Component;
import server.model.User;

/**
 * @author 张丰博
 */
@Component
public class SlowResume {

    public void slowResumeMp(User user, Potion potion) {
        // 缓慢恢复 MP  每秒恢复100
        // 计算当前mp
        user.calCurrMp();
        //设置药效时间
        potion.setUsedEndTime(System.currentTimeMillis() + PotionConst.SLOW_MP_POTION_TIME);
        // 设置药效开始时间
        potion.setUsedStartTime(System.currentTimeMillis());
        //设置自动恢复mp时间
        user.resumeMpPotionTime();
    }

    public void slowResumeHp(Potion potion) {
        //缓慢恢复 hp
        //设置药效终止时间
        potion.setUsedEndTime(System.currentTimeMillis() + PotionConst.SLOW_HP_POTION_TIME);
        //设置药效开始时间
        potion.setUsedStartTime(System.currentTimeMillis());
    }
}
