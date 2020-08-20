package server.cmdhandler.duplicate;

import com.google.protobuf.GeneratedMessageV3;
import constant.DuplicateConst;
import entity.db.UserEquipmentEntity;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import model.duplicate.BossMonster;
import model.duplicate.Duplicate;
import model.props.AbstractPropsProperty;
import model.props.Equipment;
import model.props.Potion;
import model.props.Props;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scene.GameData;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayTeam;
import server.model.User;
import server.model.UserManager;
import server.service.UserService;
import server.timer.BossAttackTimer;
import type.DuplicateType;
import type.EquipmentType;
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

        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        User user = UserManager.getUserById(userId);

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
        // 此时用户的血量
        user.calCurrHp();
        if (user.getCurrHp() <= 0) {
            // 用户阵亡，被boss打死
            // 当两个玩家同时对一个boss发起攻击，并且此boss只能承受一击时。

            return;
        }
        synchronized (currBossMonster.getATTACK_BOSS_MONITOR()) {
            if (currBossMonster.getHp() <= 0) {
                // boss已死

            }
            if (currBossMonster.getHp() <= subHp) {

                currBossMonster.setHp(0);
                // boss已死，取消定时任务
                BossAttackTimer.getInstance().cancelTask(currBossMonster.getScheduledFuture());

                // 剩余血量 小于 应减少的值 boss已死
                if (currDuplicate.getBossMonsterMap().size() > 0) {
                    // 组队进入，通知队员

                    // 此时副本中还存在boss
                    currDuplicate.setMinBoss();
                    GameMsg.NextBossResult nextBossResult = GameMsg.NextBossResult.newBuilder()
                            .setBossMonsterId(currDuplicate.getCurrBossMonster().getId())
                            .setUserId(userId)
                            .setStartTime(System.currentTimeMillis() + DuplicateConst.INIT_TIME)
                            .build();
                    sendMsg(ctx, nextBossResult, playTeam);

                } else {
                    //组队进入，通知队员

                    // 此时副本已通关，计算奖励，退出副本
                    System.out.println("计算奖励,存入数据库");

                    List<Integer> propsIdList = currDuplicate.getPropsIdList();
                    // 副本得到的奖励进行持久化
                    addProps(propsIdList, user);
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
                    userService.modifyMoney(userId, user.getMoney());
                    UserEquipmentEntity[] userEquipmentArr = user.getUserEquipmentArr();
                    Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
                    for (UserEquipmentEntity equipmentEntity : userEquipmentArr) {
                        if (equipmentEntity == null) {
                            continue;
                        }
                        Props props = propsMap.get(equipmentEntity.getPropsId());
                        if (((Equipment) props.getPropsProperty()).getEquipmentType() == EquipmentType.Weapon) {
                            // 持久化武器耐久度
                            userService.modifyEquipmentDurability(equipmentEntity.getId(), equipmentEntity.getDurability());
                            break;
                        }
                    }

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
                    GameMsg.DuplicateFinishResult duplicateFinishResult = newBuilder.setUserId(userId).build();

                    sendMsg(ctx, duplicateFinishResult, playTeam);
                }
                return;
            } else {
                // 剩余血量 大于 应减少的值
                currBossMonster.setHp(currBossMonster.getHp() - subHp);
                log.info("boss {} 受到伤害 {}, 剩余HP: {}", currBossMonster.getBossName(), subHp, currBossMonster.getHp());
                synchronized (currBossMonster.getCHOOSE_USER_MONITOR()) {
                    Map<Integer, Integer> userIdMap = currBossMonster.getUserIdMap();
                    if (!userIdMap.containsKey(userId)) {
                        userIdMap.put(userId, subHp);
                    } else {
                        Integer oldSubHp = userIdMap.get(userId);
                        userIdMap.put(userId, oldSubHp + subHp);
                    }
                }
            }
        }
        if (currBossMonster.getScheduledFuture() == null) {
            // 定时器为null,设置boss定时器， 攻击玩家
            BossAttackTimer.getInstance().bossNormalAttack(currBossMonster);
        }

        GameMsg.AttkBossResult attkBossResult = GameMsg.AttkBossResult.newBuilder().setUserId(userId).setSubHp(subHp).build();

        sendMsg(ctx, attkBossResult, playTeam);
    }


    private void sendMsg(ChannelHandlerContext ctx, GeneratedMessageV3 msg,PlayTeam playTeam){
        if (playTeam == null) {
            // 此时是个人进入副本
            ctx.writeAndFlush(msg);
        } else {
            // 此时是队伍进入副本
            Integer[] team_member = playTeam.getTEAM_MEMBER();
            for (Integer teamMemberId : team_member) {
                if (teamMemberId != null) {
                    User userById = UserManager.getUserById(teamMemberId);
                    userById.getCtx().writeAndFlush(msg);
                }
            }
        }
    }

    /**
     * 副本通关，持久化奖励
     *
     * @param propsIdList 道具id集合
     * @param user        用户
     */
    private void addProps(List<Integer> propsIdList, User user) {
        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();

        for (Integer propsId : propsIdList) {
            Props props = propsMap.get(propsId);

            try {
                if (props.getPropsProperty().getType() == PropsType.Equipment) {
                    // 添加装备到数据库;  条件不满足时有异常抛出
                    PublicMethod.getInstance().addEquipment(user, props);
                } else if (props.getPropsProperty().getType() == PropsType.Potion) {
                    PublicMethod.getInstance().addPotion(props, user, 1);
                }
                log.info("获得道具的id: {}", propsId);

            } catch (CustomizeException e) {
                log.info("获得道具失败, 道具id: {}", propsId);
                //此时给玩家发邮件
                log.error(e.getMessage(), e);
            }

        }
    }


}
