package server.cmdhandler.skill.pastor;

import constant.SkillConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.UserManager;
import server.cmdhandler.skill.ISkill;
import server.model.PlayTeam;
import server.model.User;
import server.model.profession.Skill;
import server.model.profession.skill.PastorSkillProperty;
import server.timer.ShieldTimer;
import util.MyUtil;

/**
 * @author 张丰博
 * 护盾
 */
@Component
@Slf4j
public class ShieldHandler implements ISkill {
    @Override
    public void skillHandle(ChannelHandlerContext ctx, GameMsg.UserSkillAttkCmd cmd) {
        MyUtil.checkIsNull(ctx, cmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        Skill skill = user.getSkillMap().get(cmd.getSkillId());
        PastorSkillProperty skillProperty = (PastorSkillProperty) skill.getSkillProperty();


        PlayTeam playTeam = user.getPlayTeam();
        if (playTeam != null) {
            Integer[] teamMember = playTeam.getTEAM_MEMBER();
            for (Integer id : teamMember) {
                if (id == null) {
                    continue;
                }

                User userById = UserManager.getUserById(id);
                if (userById == null) {
                    continue;
                }

                synchronized (userById.getSHIELD_MONITOR()) {
                    userById.setShieldValue(skillProperty.getShieldValue());
                }
                ShieldTimer.getInstance().cancelShield(userById, SkillConst.SHIELD_TIME);

                GameMsg.PastorSkillResult pastorSkillResult = GameMsg.PastorSkillResult.newBuilder()
                        .setShieldValue(skillProperty.getShieldValue())
                        .build();
                userById.getCtx().writeAndFlush(pastorSkillResult);
            }
        } else {
            synchronized (user.getSHIELD_MONITOR()) {
                user.setShieldValue(skillProperty.getShieldValue());
            }
            ShieldTimer.getInstance().cancelShield(user, SkillConst.SHIELD_TIME);

            GameMsg.PastorSkillResult pastorSkillResult = GameMsg.PastorSkillResult.newBuilder()
                    .setShieldValue(skillProperty.getShieldValue())
                    .build();
            user.getCtx().writeAndFlush(pastorSkillResult);
        }


        // 减蓝
        user.subMp(skill.getConsumeMp());

        log.info("用户 {} 释放 {} 增加护盾;", user.getUserName(), skill.getName());
        GameMsg.UserSkillAttkResult.Builder newBuilder = GameMsg.UserSkillAttkResult.newBuilder();
        GameMsg.UserSkillAttkResult userSkillAttkResult = newBuilder.setIsSuccess(true)
                .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                .setSubMp(skill.getConsumeMp())
                .setSubtractHp(0)
                .build();
        user.getCtx().writeAndFlush(userSkillAttkResult);

    }
}
