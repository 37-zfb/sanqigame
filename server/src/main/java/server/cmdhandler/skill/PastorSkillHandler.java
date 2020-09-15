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


        Skill skill = user.getSkillMap().get(skillId);
        PastorSkillProperty skillProperty = (PastorSkillProperty) skill.getSkillProperty();
        skill.setLastUseTime(System.currentTimeMillis());
        // 此时在场景中；并且有怪

        for (PastorSkillType skillType : PastorSkillType.values()) {
            if (!skillType.getId().equals(skillProperty.getId())) {
                continue;
            }
            try {
                Method declaredMethod = PastorSkillHandler.class.getDeclaredMethod(skillType.getName() + "Skill", User.class, Integer.class);
                declaredMethod.invoke(this, user, skillId);
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
    }

    /**
     * 治疗
     *
     * @param user
     * @param skillId
     */
    private void therapySkill(User user, Integer skillId) {
        if (user == null || skillId == null){
            return;
        }

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
                .setSubMp(skill.getConsumeMp())
                .setSubtractHp(0)
                .build();
        user.getCtx().writeAndFlush(userSkillAttkResult);
    }


}
