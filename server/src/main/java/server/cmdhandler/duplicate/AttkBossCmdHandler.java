package server.cmdhandler.duplicate;

import constant.BossMonsterConst;
import constant.DuplicateConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import server.GameServer;
import server.cmdhandler.task.listener.TaskPublicMethod;
import server.model.duplicate.BossMonster;
import server.model.duplicate.Duplicate;
import server.model.props.AbstractPropsProperty;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayTeam;
import server.model.User;
import server.service.UserService;
import server.timer.BossAttackTimer;
import type.DuplicateType;
import type.PropsType;
import util.MyUtil;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 * 普通攻击boss
 */
@Component
@Slf4j
public class AttkBossCmdHandler implements ICmdHandler<GameMsg.AttkBossCmd> {
    @Autowired
    private UserService userService;


    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.AttkBossCmd attkBossCmd) {
        MyUtil.checkIsNull(ctx, attkBossCmd);
        User user = PublicMethod.getInstance().getUser(ctx);
        // 当前副本
        Duplicate currDuplicate;

        PlayTeam playTeam = user.getPlayTeam();
        if (playTeam == null) {
            currDuplicate = user.getCurrDuplicate();
        } else {
            currDuplicate = playTeam.getCurrDuplicate();
        }

        // 计算攻击伤害
        Integer subHp = user.calMonsterSubHp();

        // 当前boss
        BossMonster currBossMonster = currDuplicate.getCurrBossMonster();

        if ((currBossMonster.getEnterRoomTime() + DuplicateConst.BOSS_TIME) < System.currentTimeMillis()) {
            // 此时副本已超时，返回公共地图
            log.error("用户: {} , 副本: {} , boss: {} , 超时;", user.getUserName(), currDuplicate.getName(), currBossMonster.getBossName());
            user.setCurrDuplicate(null);
            // 用户退出
            GameMsg.UserQuitDuplicateResult userQuitDuplicateResult =
                    GameMsg.UserQuitDuplicateResult.newBuilder().setQuitDuplicateType(DuplicateConst.USER_ABNORMAL_QUIT_DUPLICATE).build();
            ctx.writeAndFlush(userQuitDuplicateResult);
            return;
        }

        synchronized (currBossMonster.getATTACK_BOSS_MONITOR()) {
            if (currBossMonster.getHp() <= 0) {
                // boss已死
                return;
            }
            if (currBossMonster.getHp() <= subHp) {
                currBossMonster.setHp(0);
                // boss已死，取消定时任务
                BossAttackTimer.getInstance().cancelTask(currBossMonster.getScheduledFuture());
                TaskPublicMethod taskPublicMethod = GameServer.APPLICATION_CONTEXT.getBean(TaskPublicMethod.class);

                //增加经验
                taskPublicMethod.addExperience(BossMonsterConst.EXPERIENCE, user);

                // 剩余血量 小于 应减少的值 boss已死
                if (currDuplicate.getBossMonsterMap().size() > 0) {
                    // 组队进入，通知队员
                    // 此时副本中还存在boss
                    currDuplicate.setMinBoss();
                    GameMsg.NextBossResult nextBossResult = GameMsg.NextBossResult.newBuilder()
                            .setBossMonsterId(currDuplicate.getCurrBossMonster().getId())
                            .setUserId(user.getUserId())
                            .setStartTime(System.currentTimeMillis() + DuplicateConst.INIT_TIME)
                            .build();
                    PublicMethod.getInstance().sendMsg(ctx, nextBossResult);

                } else {

                    // 取消召唤师定时器
                    PublicMethod.getInstance().cancelSummonTimerOrPlayTeam(user);
                    // 增加经验
                    taskPublicMethod.addExperience(DuplicateConst.DUPLICATE_EXPERIENCE, user);
                    //组队进入，通知队员
                    // 此时副本已通关，计算奖励，退出副本
                    System.out.println("计算奖励,存入数据库");

                    List<Integer> propsIdList = currDuplicate.getPropsIdList();
                    // 副本得到的奖励进行持久化
                    PublicMethod.getInstance().addProps(propsIdList, user);
                    //背包满了，得到的奖励无法放入背包

                    GameMsg.DuplicateFinishResult.Builder newBuilder = GameMsg.DuplicateFinishResult.newBuilder();
                    for (Integer id : propsIdList) {
                        newBuilder.addPropsId(id);
                    }

                    for (DuplicateType duplicateType : DuplicateType.values()) {
                        if (duplicateType.getName().equals(currDuplicate.getName())) {
                            newBuilder.setMoney(duplicateType.getMoney());
                            user.setMoney(user.getMoney() + duplicateType.getMoney());
                        }
                    }

                    // 添加数据库,金币添加进
                    userService.modifyMoney(user.getUserId(), user.getMoney());

                    //  背包中的道具(装备、药剂等)  , 客户端更新背包中的数据
                    Map<Integer, Props> backpack = user.getBackpack();
                    for (Map.Entry<Integer, Props> propsEntry : backpack.entrySet()) {
                        GameMsg.Props.Builder propsResult = GameMsg.Props.newBuilder()
                                .setLocation(propsEntry.getKey())
                                .setPropsId(propsEntry.getValue().getId());

                        AbstractPropsProperty propsProperty = propsEntry.getValue().getPropsProperty();
                        if (propsProperty.getType() == PropsType.Equipment) {
                            Equipment equipment = (Equipment) propsProperty;
                            // equipment.getId() 是数据库中的user_equipment中的id
                            propsResult.setDurability(equipment.getDurability()).setUserPropsId(equipment.getId());
                        } else if (propsProperty.getType() == PropsType.Potion) {
                            Potion potion = (Potion) propsProperty;
                            //potion.getId() 是数据库中的 user_potion中的id
                            propsResult.setPropsNumber(potion.getNumber()).setUserPropsId(potion.getId());
                        }
                        newBuilder.addProps(propsResult);
                    }

                    GameMsg.DuplicateFinishResult duplicateFinishResult = newBuilder.setUserId(user.getUserId()).build();
                    PublicMethod.getInstance().sendMsg(ctx, duplicateFinishResult);
                    //任务监听
                    taskPublicMethod.listener(user);
                }
                return;
            } else {
                // 剩余血量 大于 应减少的值
                currBossMonster.setHp(currBossMonster.getHp() - subHp);
                log.info("boss {} 受到伤害 {}, 剩余HP: {}", currBossMonster.getBossName(), subHp, currBossMonster.getHp());
                currBossMonster.putUserIdMap(user.getUserId(), subHp);
            }
        }
        if (currBossMonster.getScheduledFuture() == null) {
            // 定时器为null,设置boss定时器， 攻击玩家
            BossAttackTimer.getInstance().bossNormalAttack(currBossMonster,user);
        }

        GameMsg.AttkBossResult attkBossResult = GameMsg.AttkBossResult.newBuilder().setUserId(user.getUserId()).setSubHp(subHp).build();
        PublicMethod.getInstance().sendMsg(ctx, attkBossResult);
    }


}
