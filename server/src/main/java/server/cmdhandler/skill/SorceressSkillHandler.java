package server.cmdhandler.skill;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import server.model.PlayArena;
import server.model.UserManager;
import server.model.duplicate.Duplicate;
import server.model.profession.Skill;
import server.model.profession.skill.SorceressSkillProperty;
import server.model.scene.Monster;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.scene.GameData;
import server.PublicMethod;
import server.model.User;
import type.skill.SorceressSkillType;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 * 法师技能处理类
 */
@Component
@Slf4j
public class SorceressSkillHandler implements ISkillHandler<SorceressSkillProperty> {
    @Override
    public void skillHandle(ChannelHandlerContext ctx, SorceressSkillProperty sorceressSkillProperty, Integer skillId) {
        User user = PublicMethod.getInstance().getUser(ctx);

        Skill skill = user.getSkillMap().get(skillId);
        SorceressSkillProperty skillProperty = (SorceressSkillProperty) skill.getSkillProperty();
        skill.setLastUseTime(System.currentTimeMillis());
        // 此时在场景中；并且有怪

        // 此时在副本中
        for (SorceressSkillType skillType : SorceressSkillType.values()) {
            if (!skillType.getId().equals(skillProperty.getId())) {
                continue;
            }
            try {
                Method declaredMethod = SorceressSkillHandler.class.getDeclaredMethod(skillType.getName() + "Skill", User.class, Integer.class);
                declaredMethod.invoke(this, user, skillId);

                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 法师技能攻击，攻击所有的怪
     *
     * @param user
     * @param skillId
     */
    private void allAttackSkill(User user, Integer skillId) {
        if (user == null || skillId == null) {
            return;
        }
        //先判断是否有副本
        Duplicate currDuplicate = PublicMethod.getInstance().getDuplicate(user);
        //竞技场
        PlayArena playArena = user.getPlayArena();
        // 在公共地图
        Map<Integer, Monster> monsterMap = GameData.getInstance().getSceneMap().get(user.getCurSceneId()).getMonsterMap();

        Skill skill = user.getSkillMap().get(skillId);
        SorceressSkillProperty skillProperty = (SorceressSkillProperty) skill.getSkillProperty();

        int equDamage = user.getEquDamage();
        int subHp = (int) (Math.random() * (user.getBaseDamage() * skillProperty.getDamagePercent() + skillProperty.getDamageValue() + user.getBaseDamage()) + equDamage * 10) + 500;


        if (currDuplicate != null) {
            PublicMethod.getInstance().normalOrSkillAttackBoss(user, currDuplicate, subHp, null);
        } else if (playArena != null) {

            Integer targetUserId = playArena.getTargetUserId();
            User targetUser = UserManager.getUserById(targetUserId);
            if (targetUser == null){
                return;
            }

            synchronized (targetUser.getHpMonitor()){
                targetUser.setCurrHp(targetUser.getCurrHp()-subHp);
            }

        } else if (monsterMap.size() != 0) {
            // 存活着的怪
            List<Monster> monsterAliveList = PublicMethod.getInstance().getMonsterAliveList(monsterMap.values());
            if (monsterAliveList == null){
                return;
            }

            for (Monster monster : monsterAliveList) {
                PublicMethod.getInstance().userOrSummonerAttackMonster(user, monster, null, subHp);
            }

        }

        // 减蓝
        user.subMp(skill.getConsumeMp());

        log.info("用户 {} 释放 {} 攻击全部;", user.getUserName(), skill.getName());
        GameMsg.UserSkillAttkResult.Builder newBuilder = GameMsg.UserSkillAttkResult.newBuilder();
        GameMsg.UserSkillAttkResult userSkillAttkResult = newBuilder.setIsSuccess(true)
                .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                .setSubtractHp(0)
                .build();
        user.getCtx().writeAndFlush(userSkillAttkResult);
    }
}
