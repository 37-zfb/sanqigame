package server.cmdhandler.skillhandler;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import model.duplicate.Duplicate;
import model.profession.Skill;
import model.profession.skill.PastorSkillProperty;
import model.scene.Monster;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import scene.GameData;
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
    public void skillHandle(ChannelHandlerContext ctx, PastorSkillProperty pastorSkillProperty,Integer skillId) {

        User user = PublicMethod.getInstance().getUser(ctx);

        //先判断是否有副本
        Duplicate currDuplicate  = PublicMethod.getInstance().getPlayTeam(user);

        Skill skill = user.getSkillMap().get(skillId);
        PastorSkillProperty skillProperty = (PastorSkillProperty) skill.getSkillProperty();
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
            for (PastorSkillType skillType : PastorSkillType.values()) {
                if (skillType.getId().equals(skillProperty.getId())) {
                    try {
                        Method declaredMethod = PastorSkillHandler.class.getDeclaredMethod(skillType.getName() + "Skill", User.class, Duplicate.class, Integer.class);
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
     *  治疗
     * @param user
     * @param duplicate
     * @param skillId
     */
    private void therapySkill(User user, Duplicate duplicate, Integer skillId) {
        Skill skill = user.getSkillMap().get(skillId);
        PastorSkillProperty skillProperty = (PastorSkillProperty) skill.getSkillProperty();

        skill.setLastUseTime(System.currentTimeMillis());
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
