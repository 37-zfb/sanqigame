package server.cmdhandler.skill;

import exception.CustomizeErrorCode;
import exception.CustomizeException;
import server.PublicMethod;
import server.model.PlayArena;
import server.model.User;
import server.model.duplicate.Duplicate;
import server.model.scene.Monster;
import server.scene.GameData;

import java.util.Map;

/**
 * @author 张丰博
 * 技能工具类
 */
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

}
