package server.cmdhandler.skillhandler;

import constant.ProfessionConst;
import constant.SkillConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import model.duplicate.Duplicate;
import model.profession.Skill;
import model.profession.SummonMonster;
import model.profession.skill.SummonerSkillProperty;
import model.profession.skill.WarriorSkillProperty;
import model.scene.Monster;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import scene.GameData;
import server.PublicMethod;
import server.model.User;
import server.timer.SummonMonsterTimer;
import type.skill.SummonerSkillType;
import type.skill.WarriorSkillType;

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
    public void skillHandle(ChannelHandlerContext ctx, SummonerSkillProperty summonerSkillProperty,Integer skillId) {

        User user = PublicMethod.getInstance().getUser(ctx);

        //先判断是否有副本
        Duplicate currDuplicate  = PublicMethod.getInstance().getPlayTeam(user);

        Skill skill = user.getSkillMap().get(skillId);
        SummonerSkillProperty skillProperty = (SummonerSkillProperty) skill.getSkillProperty();
        if (currDuplicate == null) {
            // 此时在场景中；并且有怪
            // 在公共地图
            Map<Integer, Monster> monsterMap = GameData.getInstance().getSceneMap().get(user.getCurSceneId()).getMonsterMap();
            // 存活着的怪
            List<Monster> monsterAliveList = PublicMethod.getInstance().getMonsterAliveList(monsterMap.values());
            if (monsterAliveList.size() != 0) {
                // 嘲讽当前场景中所有的怪
                for (Monster monster : monsterAliveList) {
                    // 后端架构如果采用多线程，怪减血时要加synchronized
                    synchronized (monster.getSubHpMonitor()) {
                        if (monster.isDie()) {
                            // 有可能刚被前一用户杀死，
                            // 怪死，减蓝、技能设为cd; 重新定义恢复终止时间
                            user.subMp(skill.getConsumeMp());
                            log.info("{} 已被其他玩家击杀!", monster.getName());
                            GameMsg.DieResult dieResult = GameMsg.DieResult.newBuilder()
                                    .setMonsterId(monster.getId())
                                    .setIsDieBefore(true)
                                    .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                                    .build();
                            user.getCtx().channel().writeAndFlush(dieResult);
                        } else {


//                            log.info("玩家:{},使:{} 减血 {}!", user.getUserName(), monster.getName(), subHp);
                        }
                    }
                    // 减蓝
                    user.subMp(skill.getConsumeMp());
                }
            } else {
                //当前场景中的怪全死了

            }


        } else {
            // 此时在副本中
            for (SummonerSkillType skillType : SummonerSkillType.values()) {
                if (skillType.getId().equals(skillProperty.getId())) {
                    try {
                        Method declaredMethod = SummonerSkillHandler.class.getDeclaredMethod(skillType.getName() + "Skill", User.class, Duplicate.class, Integer.class);
                        declaredMethod.invoke(this, user, currDuplicate, skillId);
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }


    }

    /**
     *  召唤术；
     * @param user
     * @param duplicate
     * @param skillId
     */
    private void summonSkill(User user, Duplicate duplicate, Integer skillId) {
        Skill skill = user.getSkillMap().get(skillId);
        SummonerSkillProperty skillProperty = (SummonerSkillProperty) skill.getSkillProperty();

        skill.setLastUseTime(System.currentTimeMillis());

        SummonMonster summonMonster = new SummonMonster();
        summonMonster.setSkillId(skillProperty.getSkillId());
        summonMonster.setStartTime(System.currentTimeMillis());
        summonMonster.setEndTime(System.currentTimeMillis()+skillProperty.getEffectTime()* SkillConst.CD_UNIt_SWITCH);
        summonMonster.setHp((int) (ProfessionConst.HP*0.5));
        summonMonster.setDamage(skillProperty.getPetDamage());
        summonMonster.setCtx(user.getCtx());

        Map<Integer, SummonMonster> summonMonsterMap = user.getSummonMonsterMap();
        summonMonsterMap.put(skillProperty.getId(), summonMonster);

        // 减蓝
        user.subMp(skill.getConsumeMp());
        SummonMonsterTimer.getInstance().startAttack(user,duplicate,summonMonster);
        SummonMonsterTimer.getInstance().cancelTimer(user, summonMonster,skillProperty.getEffectTime());
        //启动定时器，攻击当前副本中的boss
        log.info("用户 {} ,使用召唤术",user.getUserName());
        GameMsg.UserSkillAttkResult.Builder newBuilder = GameMsg.UserSkillAttkResult.newBuilder();
        GameMsg.UserSkillAttkResult userSkillAttkResult = newBuilder.setIsSuccess(true)
                .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                .setSubtractHp(0)
                .build();
        user.getCtx().writeAndFlush(userSkillAttkResult);

    }


}
