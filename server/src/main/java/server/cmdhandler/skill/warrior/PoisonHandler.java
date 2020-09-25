package server.cmdhandler.skill.warrior;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.skill.ISkill;
import server.cmdhandler.skill.SkillUtil;
import server.cmdhandler.task.listener.TaskUtil;
import server.model.User;
import server.model.profession.Skill;
import server.timer.PoisonTimer;
import util.MyUtil;

/**
 * @author 张丰博
 * 放毒
 */
@Component
@Slf4j
public class PoisonHandler implements ISkill {


    @Override
    public void skillHandle(ChannelHandlerContext ctx, GameMsg.UserSkillAttkCmd cmd) {
        MyUtil.checkIsNull(ctx, cmd);
        User user = PublicMethod.getInstance().getUser(ctx);



        Skill skill = user.getSkillMap().get(cmd.getSkillId());

        PoisonTimer.getInstance().poisonSkill(user,cmd.getSkillId());

        // 减蓝
        user.subMp(skill.getConsumeMp());

        log.info("用户 {} 释放 {} ;", user.getUserName(), skill.getName());
        GameMsg.UserSkillAttkResult.Builder newBuilder = GameMsg.UserSkillAttkResult.newBuilder();
        GameMsg.UserSkillAttkResult userSkillAttkResult = newBuilder.setIsSuccess(true)
                .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                .setSubMp(skill.getConsumeMp())
                .setSubtractHp(0)
                .build();
        user.getCtx().writeAndFlush(userSkillAttkResult);

    }
}
