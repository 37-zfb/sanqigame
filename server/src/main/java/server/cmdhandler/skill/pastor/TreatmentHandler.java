package server.cmdhandler.skill.pastor;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.skill.ISkill;
import server.model.User;
import server.model.profession.Skill;
import server.model.profession.skill.PastorSkillProperty;
import server.timer.PastorSkillTimer;
import util.MyUtil;

import java.util.concurrent.RunnableScheduledFuture;

/**
 * @author 张丰博
 *
 * 治疗
 *
 */
@Component
@Slf4j
public class TreatmentHandler implements ISkill {
    @Override
    public void skillHandle(ChannelHandlerContext ctx, GameMsg.UserSkillAttkCmd cmd) {
        MyUtil.checkIsNull(ctx, cmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        Skill skill = user.getSkillMap().get(cmd.getSkillId());
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
