package server.cmdhandler.skill.summoner;

import constant.ProfessionConst;
import constant.SkillConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.skill.ISkill;
import server.model.User;
import server.model.profession.Skill;
import server.model.profession.SummonMonster;
import server.model.profession.skill.SummonerSkillProperty;
import server.timer.SummonMonsterTimer;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 * 召唤术
 */
@Component
@Slf4j
public class SummonHandler implements ISkill {
    @Override
    public void skillHandle(ChannelHandlerContext ctx, GameMsg.UserSkillAttkCmd cmd) {

        MyUtil.checkIsNull(ctx, cmd);
        User user = PublicMethod.getInstance().getUser(ctx);


        Skill skill = user.getSkillMap().get(cmd.getSkillId());
        SummonerSkillProperty skillProperty = (SummonerSkillProperty) skill.getSkillProperty();

        SummonMonster summonMonster = new SummonMonster();
        summonMonster.setSkillId(skillProperty.getSkillId());
        summonMonster.setName("召唤兽");
        summonMonster.setStartTime(System.currentTimeMillis());
        summonMonster.setEndTime(System.currentTimeMillis() + skillProperty.getEffectTime() * SkillConst.CD_UNIt_SWITCH);
        summonMonster.setHp((int) (ProfessionConst.HP * 0.5));
        summonMonster.setDamage(skillProperty.getPetDamage());
        summonMonster.setCtx(user.getCtx());

        Map<Integer, SummonMonster> summonMonsterMap = user.getSummonMonsterMap();
        summonMonsterMap.put(skillProperty.getId(), summonMonster);

        // 减蓝
        user.subMp(skill.getConsumeMp());
        SummonMonsterTimer.getInstance().startAttack(user, summonMonster);

        SummonMonsterTimer.getInstance().cancelTimer(user, summonMonster, skillProperty.getEffectTime());

        log.info("用户 {} ,使用召唤术", user.getUserName());
        GameMsg.UserSkillAttkResult.Builder newBuilder = GameMsg.UserSkillAttkResult.newBuilder();
        GameMsg.UserSkillAttkResult userSkillAttkResult = newBuilder.setIsSuccess(true)
                .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                .setSubMp(skill.getConsumeMp())
                .setSubtractHp(0)
                .build();
        user.getCtx().writeAndFlush(userSkillAttkResult);
    }
}
