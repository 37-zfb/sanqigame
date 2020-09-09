package server.cmdhandler.skill;

import constant.ProfessionConst;
import constant.SkillConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import server.model.duplicate.Duplicate;
import server.model.profession.Skill;
import server.model.profession.SummonMonster;
import server.model.profession.skill.SummonerSkillProperty;
import server.model.scene.Monster;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.scene.GameData;
import server.PublicMethod;
import server.model.User;
import server.timer.SummonMonsterTimer;
import type.skill.SummonerSkillType;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 * 召唤师技能处理类
 */
@Component
@Slf4j
public class SummonerSkillHandler implements ISkillHandler<SummonerSkillProperty> {
    @Override
    public void skillHandle(ChannelHandlerContext ctx, SummonerSkillProperty summonerSkillProperty, Integer skillId) {

        User user = PublicMethod.getInstance().getUser(ctx);

        //先判断是否有副本
        Duplicate currDuplicate = PublicMethod.getInstance().getDuplicate(user);

        Skill skill = user.getSkillMap().get(skillId);
        SummonerSkillProperty skillProperty = (SummonerSkillProperty) skill.getSkillProperty();
        skill.setLastUseTime(System.currentTimeMillis());
        Map<Integer, Monster> monsterMap = GameData.getInstance().getSceneMap().get(user.getCurSceneId()).getMonsterMap();
        // 存活着的怪
        List<Monster> monsterAliveList = PublicMethod.getInstance().getMonsterAliveList(monsterMap.values());

        // 此时在副本中
        for (SummonerSkillType skillType : SummonerSkillType.values()) {
            if (!skillType.getId().equals(skillProperty.getId())) {
                continue;
            }
            try {
                Method declaredMethod;
                if (currDuplicate == null) {
                    // 公共地图
                    declaredMethod = SummonerSkillHandler.class.getDeclaredMethod(skillType.getName() + "SkillScene", List.class, User.class, Integer.class);
                    declaredMethod.invoke(this, monsterAliveList, user, skillId);
                } else {
                    // 场景
                    declaredMethod = SummonerSkillHandler.class.getDeclaredMethod(skillType.getName() + "Skill", User.class, Integer.class);
                    declaredMethod.invoke(this, user, skillId);
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
    private void summonSkillScene(List<Monster> monsterAliveList, User user, Integer skillId) {
        summonSkill(user, skillId);
    }


    /**
     * 召唤术；
     *
     * @param user
     * @param skillId
     */
    private void summonSkill(User user, Integer skillId) {
        Skill skill = user.getSkillMap().get(skillId);
        SummonerSkillProperty skillProperty = (SummonerSkillProperty) skill.getSkillProperty();

        SummonMonster summonMonster = new SummonMonster();
        summonMonster.setSkillId(skillProperty.getSkillId());
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
        //启动定时器，攻击当前副本中的boss
        log.info("用户 {} ,使用召唤术", user.getUserName());
        GameMsg.UserSkillAttkResult.Builder newBuilder = GameMsg.UserSkillAttkResult.newBuilder();
        GameMsg.UserSkillAttkResult userSkillAttkResult = newBuilder.setIsSuccess(true)
                .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                .setSubtractHp(0)
                .build();
        user.getCtx().writeAndFlush(userSkillAttkResult);

    }


}
