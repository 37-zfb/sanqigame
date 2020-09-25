package server.cmdhandler.skill;

import constant.DuplicateConst;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import lombok.extern.slf4j.Slf4j;
import server.PublicMethod;
import server.model.PlayArena;
import server.model.User;
import server.model.duplicate.BossMonster;
import server.model.duplicate.Duplicate;
import server.model.scene.Monster;
import server.scene.GameData;
import server.timer.BossAttackTimer;

import java.util.Map;

/**
 * @author 张丰博
 * 技能工具类
 */
@Slf4j
public class SkillUtil {
    private SkillUtil(){}

    private static final SkillUtil SKILL_UTIL = new SkillUtil();

    public static SkillUtil getSkillUtil(){
        return SKILL_UTIL;
    }

    public void skillDestination(User user){
        //先判断是否有副本
        Duplicate currDuplicate = PublicMethod.getInstance().getDuplicate(user);

        //竞技场
        PlayArena playArena = user.getPlayArena();

        // 此时在野外
        Map<Integer, Monster> monsterMap = GameData.getInstance().getSceneMap().get(user.getCurSceneId()).getMonsterMap();
        if (currDuplicate == null && playArena == null && monsterMap.size() == 0){
            //此时无效
            throw new CustomizeException(CustomizeErrorCode.SCENE_NOT_MONSTER);
        }
    }


    public void isTimeout(User user, BossMonster currBossMonster){

        if (user == null || currBossMonster == null){
            return;
        }

        Duplicate currDuplicate = PublicMethod.getInstance().getDuplicate(user);
        if ((currBossMonster.getEnterRoomTime() + DuplicateConst.BOSS_TIME) < System.currentTimeMillis()) {
            // 副本超时
            log.error("用户: {} , 副本: {} , boss: {} , 超时;", user.getUserName(), currDuplicate.getName(), currBossMonster.getBossName());

            BossAttackTimer.getInstance().cancelTask(currDuplicate.getCurrBossMonster().getScheduledFuture());
            PublicMethod.getInstance().cancelSummonTimerOrPlayTeam(user);

            //持久化装备耐久度
            PublicMethod.getInstance().dbWeaponDurability(user.getUserEquipmentArr());

            if (user.getPlayTeam() == null){
                user.setCurrDuplicate(null);
            }else {
                user.getPlayTeam().setCurrDuplicate(null);
            }

            // 用户退出
            throw new CustomizeException(CustomizeErrorCode.DUPLICATE_TIME_OUT);
        }
    }

}
