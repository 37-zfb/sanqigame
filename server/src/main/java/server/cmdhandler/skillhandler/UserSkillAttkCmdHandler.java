package server.cmdhandler.skillhandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import model.profession.Skill;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import entity.db.UserEquipmentEntity;
import scene.GameData;
import server.cmdhandler.ICmdHandler;
import server.model.*;
import model.props.Equipment;
import model.scene.Monster;
import model.scene.Scene;
import server.service.UserService;
import type.EquipmentType;
import server.Broadcast;

import java.util.*;

/**
 * 技能攻击怪
 *
 * @author 张丰博
 */
@Slf4j
@Component
public class UserSkillAttkCmdHandler implements ICmdHandler<GameMsg.UserSkillAttkCmd> {
    @Autowired
    private UserService userService;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserSkillAttkCmd cmd) {

        if (ctx == null || cmd == null) {
            return;
        }

        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        User user = UserManager.getUserById(userId);
        if (user == null) {
            return;
        }
        // 用户当前场景
        Scene scene = GameData.getInstance().getSceneMap().get(user.getCurSceneId());

        // 当前场景所有怪
        Map<Integer, Monster> monsterMap = scene.getMonsterMap();
        GameMsg.UserSkillAttkResult.Builder resultBuilder = GameMsg.UserSkillAttkResult.newBuilder();
        if (monsterMap.size() == 0) {
            // 当前场景没有怪
            log.info("场景: {} 没有怪;", scene.getName());
            GameMsg.UserSkillAttkResult skillAttkResult = resultBuilder.setIsSuccess(false).build();
            ctx.channel().writeAndFlush(skillAttkResult);
            return;
        }

        //存活的 怪
        List<Monster> monsterAliveList = getMonsterAliveList(monsterMap.values());
        Skill skill = user.getSkillMap().get(cmd.getSkillId());

        if (skill != null) {
            // 计算当前mp值
            user.calCurrMp();
            user.resumeMpTime();
            // 判断当前 技能是否 冷却状态
            if (skill.isCd()) {
                log.info("cd冷却中!");
                resultBuilder.setIsSuccess(false)
                        .setFalseReason("cd");
            } else if (user.getCurrMp() < skill.getConsumeMp()) {
                log.info("mp不足!");
                resultBuilder.setIsSuccess(false)
                        .setFalseReason("mp");
            } else if (monsterAliveList.size() != 0) {
                // 使用技能
                // 计算伤害，攻击对应的怪；

                // 随机选中一个怪
                Monster monster = monsterAliveList.remove((int) (Math.random() * monsterAliveList.size()));
                // 减血  (0~99) + 500
                int subHp = user.calMonsterSubHp(skill);
                //持久化装备耐久度
                UserEquipmentEntity[] userEquipmentArr = user.getUserEquipmentArr();
//                for (int i = 0; i < userEquipmentArr.length; i++) {
//                    if (userEquipmentArr[i] != null && equipmentMap.get(userEquipmentArr[i].getPropsId()).getName().equals(EquipmentType.Weapon.getType())) {
//                        userService.modifyEquipmentDurability(userEquipmentArr[i].getId(), userEquipmentArr[i].getDurability());
//                    }
//                }


                // 后端架构如果采用多线程，怪减血时要加synchronized
                synchronized (monster.getSubHpMontor()) {

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
                        ctx.channel().writeAndFlush(dieResult);
                        return;
                    } else if (monster.getHp() <= subHp) {
                        // 死亡
                        monster.setHp(0);
                        log.info("玩家:{},击杀:{}!", user.getUserName(), monster.getName());
                        // 用户减蓝,重新设置状态终止时间
                        user.subMp(skill.getConsumeMp());

                        // 爆道具
                        GameMsg.DieResult.Builder dieBuilder = GameMsg.DieResult.newBuilder();

                        Integer propsId = null;
                        Random random = new Random();
                        int nextInt = random.nextInt(30);
                        if (nextInt < 10) {
//                            propsId = monster.createEquipment();
                            dieBuilder.setPropsType("0");
                        } else {
//                            propsId = monster.createPotion();
                            dieBuilder.setPropsType("1");
                        }


                        GameMsg.DieResult dieResult = dieBuilder.setMonsterId(monster.getId())
                                .setIsDieBefore(false)
                                .setPropsId(propsId)
                                .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                                .build();

                        Broadcast.broadcast(user.getCurSceneId(), dieResult);
                        return;
                    } else {
                        // 减血
                        monster.setHp(monster.getHp() - subHp);

                        if (skill.getId() == 2) {
                            // 十字斩，有出血功能
                            monster.startDropHp(ctx);
                        }

                        log.info("玩家:{},使:{} 减血 {}!", user.getUserName(), monster.getName(), subHp);
                    }
                }
                // 减蓝
                user.subMp(skill.getConsumeMp());

                resultBuilder.setIsSuccess(true)
                        .setSubtractHp(subHp)
                        .setMonsterId(monster.getId())
                        .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp());

                GameMsg.UserSkillAttkResult attkResult = resultBuilder.build();
                Broadcast.broadcast(user.getCurSceneId(), attkResult);
                return;

            } else if (monsterMap.size() != 0) {
                log.info("{} 怪全死了!", scene.getName());
                // 减mp
                user.subMp(skill.getConsumeMp());
                // 怪全死
                resultBuilder.setIsSuccess(false)
                        .setFalseReason("no");
            }


        }

        // 响应客户端，是否成功
        GameMsg.UserSkillAttkResult userSkillAttkResult = resultBuilder.build();

        ctx.channel().writeAndFlush(userSkillAttkResult);
    }


    /**
     * 返回还存活的怪集合
     *
     * @param monsterList 所有怪的集合
     * @return 存活怪的集合
     */
    private List<Monster> getMonsterAliveList(Collection<Monster> monsterList) {
        List<Monster> monsterAliveList = new ArrayList<>();
        //存活的 怪
        for (Monster monster : monsterList) {
            if (!monster.isDie()) {
                monsterAliveList.add(monster);
            }
        }
        return monsterAliveList;
    }

}
