package server.cmdhandler.skill;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import server.model.duplicate.Duplicate;
import server.model.profession.Skill;
import server.model.profession.skill.PastorSkillProperty;
import server.model.scene.Monster;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.scene.GameData;
import server.PublicMethod;
import server.model.User;
import server.timer.PastorSkillTimer;
import type.skill.PastorSkillType;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RunnableScheduledFuture;

/**
 * @author 张丰博
 * 牧师技能处理类
 */
@Component
@Slf4j
public class PastorSkillHandler implements ISkillHandler<PastorSkillProperty> {
    @Override
    public void skillHandle(ChannelHandlerContext ctx, PastorSkillProperty pastorSkillProperty, Integer skillId) {

        User user = PublicMethod.getInstance().getUser(ctx);

        //先判断是否有副本
        Duplicate currDuplicate = PublicMethod.getInstance().getDuplicate(user);

        Skill skill = user.getSkillMap().get(skillId);
        skill.setLastUseTime(System.currentTimeMillis());
        PastorSkillProperty skillProperty = (PastorSkillProperty) skill.getSkillProperty();
        // 此时在场景中；并且有怪

        // 在公共地图
        Map<Integer, Monster> monsterMap = GameData.getInstance().getSceneMap().get(user.getCurSceneId()).getMonsterMap();
        // 存活着的怪
        List<Monster> monsterAliveList = PublicMethod.getInstance().getMonsterAliveList(monsterMap.values());
        for (PastorSkillType skillType : PastorSkillType.values()) {
            if (!skillType.getId().equals(skillProperty.getId())) {
                continue;
            }
            try {
                Method declaredMethod;
                if (currDuplicate == null) {
                    // 在公共地图中
                    // 存活着的怪
                    declaredMethod = PastorSkillHandler.class.getDeclaredMethod(skillType.getName() + "SkillScene", List.class, User.class, Integer.class);
                    declaredMethod.invoke(this, monsterAliveList, user, skillId);
                } else {
                    // 副本中

                    declaredMethod = PastorSkillHandler.class.getDeclaredMethod(skillType.getName() + "Skill", User.class, Duplicate.class, Integer.class);
                    declaredMethod.invoke(this, user, currDuplicate, skillId);
                }
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    /**
     * 治疗术
     *
     * @param monsterAliveList 存活怪的集合
     * @param user             用户对象
     * @param skillId          技能id
     */
    private void therapySkillScene(List<Monster> monsterAliveList, User user, Integer skillId) {
        therapySkill(user, user.getCurrDuplicate(), skillId);
    }

    /**
     * 治疗
     *
     * @param user
     * @param duplicate
     * @param skillId
     */
    private void therapySkill(User user, Duplicate duplicate, Integer skillId) {
        Skill skill = user.getSkillMap().get(skillId);
        PastorSkillProperty skillProperty = (PastorSkillProperty) skill.getSkillProperty();

        //治疗
        RunnableScheduledFuture<?> runnableScheduledFuture = PastorSkillTimer.getInstance().userChant(user, skillProperty);
        user.setIsPrepare(runnableScheduledFuture);

        // 减蓝
        user.subMp(skill.getConsumeMp());

        log.info("用户 {} 释放 {} 全体成员恢复状态;", user.getUserName(), skill.getName());
        GameMsg.UserSkillAttkResult.Builder newBuilder = GameMsg.UserSkillAttkResult.newBuilder();
        GameMsg.UserSkillAttkResult userSkillAttkResult = newBuilder.setIsSuccess(true)
                .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                .setSubtractHp(0)
                .build();
        user.getCtx().writeAndFlush(userSkillAttkResult);
    }


}
