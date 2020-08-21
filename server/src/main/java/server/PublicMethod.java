package server;

import com.google.protobuf.GeneratedMessageV3;
import constant.*;
import entity.db.UserEquipmentEntity;
import entity.db.UserPotionEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import model.duplicate.BossMonster;
import model.duplicate.Duplicate;
import model.duplicate.ForceAttackUser;
import model.profession.SummonMonster;
import model.props.AbstractPropsProperty;
import model.props.Equipment;
import model.props.Potion;
import model.props.Props;
import model.scene.Monster;
import msg.GameMsg;
import scene.GameData;
import server.model.PlayTeam;
import server.model.User;
import server.model.UserManager;
import server.service.UserService;
import server.timer.BossAttackTimer;
import type.DuplicateType;
import type.EquipmentType;
import type.PropsType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 */
@Slf4j
public final class PublicMethod {

    private static final PublicMethod PUBLIC_METHOD = new PublicMethod();

    private UserService userService = GameServer.APPLICATION_CONTEXT.getBean(UserService.class);

    private PublicMethod() {
    }

    public static PublicMethod getInstance() {
        return PUBLIC_METHOD;
    }


    /**
     * 用户普通攻击或者带伤害的技能攻击
     *
     * @param user
     * @param currDuplicate
     * @param subHp
     */
    public void normalOrSkillAttackBoss(User user, Duplicate currDuplicate, Integer subHp, SummonMonster summonMonster) {

        PlayTeam playTeam = user.getPlayTeam();
        BossMonster currBossMonster = currDuplicate.getCurrBossMonster();

        GameMsg.AttkBossResult.Builder newBuilder1 = GameMsg.AttkBossResult.newBuilder();

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
                            .setUserId(user.getUserId())
                            .setStartTime(System.currentTimeMillis() + DuplicateConst.INIT_TIME)
                            .build();
                    PublicMethod.getInstance().sendMsg(user.getCtx(), nextBossResult, playTeam);

                } else {
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
                    GameMsg.DuplicateFinishResult duplicateFinishResult = newBuilder.setUserId(user.getUserId()).build();

                    PublicMethod.getInstance().sendMsg(user.getCtx(), duplicateFinishResult, playTeam);
                }
                return;
            } else {
                // 剩余血量 大于 应减少的值
                currBossMonster.setHp(currBossMonster.getHp() - subHp);
                log.info("boss {} 受到伤害 {}, 剩余HP: {}", currBossMonster.getBossName(), subHp, currBossMonster.getHp());
                synchronized (currBossMonster.getCHOOSE_USER_MONITOR()) {
                    if (summonMonster == null) {
                        Map<Integer, Integer> userIdMap = currBossMonster.getUserIdMap();
                        if (!userIdMap.containsKey(user.getUserId())) {
                            userIdMap.put(user.getUserId(), subHp);
                        } else {
                            Integer oldSubHp = userIdMap.get(user.getUserId());
                            userIdMap.put(user.getUserId(), oldSubHp + subHp);
                        }
                    } else {
                        // 召唤兽
                        Map<SummonMonster, Integer> summonMonsterMap = currBossMonster.getSummonMonsterMap();
                        if (!summonMonsterMap.containsKey(summonMonster)) {
                            summonMonsterMap.put(summonMonster, subHp);
                        } else {
                            Integer oldSubHp = summonMonsterMap.get(summonMonster);
                            summonMonsterMap.put(summonMonster, oldSubHp + subHp);
                        }
                        newBuilder1.setType("召唤兽");
                    }

                }
            }

        }

        if (currBossMonster.getScheduledFuture() == null) {
            // 定时器为null,设置boss定时器， 攻击玩家
            BossAttackTimer.getInstance().bossNormalAttack(currBossMonster);
        }

        GameMsg.AttkBossResult attkBossResult = newBuilder1.setUserId(user.getUserId()).setSubHp(subHp).build();

        PublicMethod.getInstance().sendMsg(user.getCtx(), attkBossResult, playTeam);
    }

    /**
     * 副本通关，持久化奖励
     *
     * @param propsIdList 道具id集合
     * @param user        用户
     */
    public void addProps(List<Integer> propsIdList, User user) {
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
            } catch (NullPointerException e) {
                log.info("获得道具失败, 道具id: {}", propsId);
                //此时给玩家发邮件
                log.error(e.getMessage(), e);
            }

        }
    }

    /**
     * 发送数据
     *
     * @param ctx
     * @param msg
     * @param playTeam
     */
    public void sendMsg(ChannelHandlerContext ctx, GeneratedMessageV3 msg, PlayTeam playTeam) {
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
     * 添加装备
     *
     * @param user
     * @param props
     * @throws CustomizeException 如果背包满了，则抛出异常
     */
    public void addEquipment(User user, Props props) {

        Map<Integer, Props> backpack = user.getBackpack();

        if (backpack.size() >= BackPackConst.MAX_CAPACITY) {
            // 此时背包已满，
            throw new CustomizeException(CustomizeErrorCode.BACKPACK_SPACE_INSUFFICIENT);
        }

        // 此道具是 装备
        Equipment equipment = (Equipment) props.getPropsProperty();

        //封装
        UserEquipmentEntity userEquipmentEntity = new UserEquipmentEntity();
        userEquipmentEntity.setIsWear(EquipmentConst.NO_WEAR);
        userEquipmentEntity.setDurability(EquipmentConst.MAX_DURABILITY);
        userEquipmentEntity.setPropsId(equipment.getPropsId());
        userEquipmentEntity.setUserId(user.getUserId());

        Equipment equ = null;
        for (int i = 1; i < BackPackConst.MAX_CAPACITY; i++) {
            if (!backpack.keySet().contains(i)) {
                Props pro = new Props();
                pro.setId(equipment.getPropsId());
                pro.setName(props.getName());
                equ = new Equipment(null, pro.getId(), EquipmentConst.MAX_DURABILITY, equipment.getDamage(), equipment.getEquipmentType());
                pro.setPropsProperty(equ);

                backpack.put(i, pro);
                userEquipmentEntity.setLocation(i);
                break;
            }
        }
        userService.addEquipment(userEquipmentEntity);
        equ.setId(userEquipmentEntity.getId());

    }

    /**
     * 添加药剂
     *
     * @param props
     * @param user
     * @param number
     * @throws CustomizeException 如果背包中不存在此药剂且背包满了，则抛出异常；背包中此药剂数量达到上限抛出异常
     */
    public void addPotion(Props props, User user, Integer number) {

        Map<Integer, Props> backpack = user.getBackpack();

        // 此道具是 药剂
        Potion potion = (Potion) props.getPropsProperty();

        UserPotionEntity userPotionEntity = new UserPotionEntity();
        userPotionEntity.setUserId(user.getUserId());
        userPotionEntity.setPropsId(potion.getPropsId());

        boolean isExist = false;
        for (Props pro : backpack.values()) {
            // 查询背包中是否有该药剂
            if (potion.getPropsId().equals(pro.getId())) {
                // 判断该药剂的数量是否达到上限
                // 背包中已有该药剂
                Potion po = (Potion) pro.getPropsProperty();

                if ((po.getNumber() + number) > PotionConst.POTION_MAX_NUMBER) {
                    throw new CustomizeException(CustomizeErrorCode.PROPS_REACH_LIMIT);
                }

                po.setNumber(po.getNumber() + number);
                isExist = true;

                userPotionEntity.setNumber(po.getNumber());
                break;
            }
        }
        // 背包中还没有该药剂
        if (!isExist) {
            if (backpack.size() >= BackPackConst.MAX_CAPACITY) {
                // 此时背包已满，
                throw new CustomizeException(CustomizeErrorCode.BACKPACK_SPACE_INSUFFICIENT);
            }

            userPotionEntity.setNumber(number);

            Potion po = null;
            for (int i = 1; i < BackPackConst.MAX_CAPACITY; i++) {
                if (!backpack.keySet().contains(i)) {
                    Props pro = new Props();
                    pro.setId(potion.getPropsId());
                    pro.setName(props.getName());
                    po = new Potion(null, potion.getPropsId(), potion.getCdTime(), potion.getInfo(), potion.getResumeFigure(), potion.getPercent(), number);
                    pro.setPropsProperty(po);

                    userPotionEntity.setLocation(i);
                    // 药剂添加进背包
                    backpack.put(i, pro);
                    break;
                }
            }
        }
        userService.addPotion(userPotionEntity);
        userPotionEntity.setId(userPotionEntity.getId());
    }


    /**
     * 获取用户
     *
     * @param ctx
     * @return
     */
    public User getUser(ChannelHandlerContext ctx) {
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        if (userId == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_MANAGER);
        }
        User user = UserManager.getUserById(userId);
        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_MANAGER);
        }

        return user;
    }

    /**
     * 退出队伍
     *
     * @param user
     */
    public void quitTeam(User user) {
        PlayTeam playTeam = user.getPlayTeam();
        if (playTeam == null) {
            return;
        }
        Integer[] team_member = playTeam.getTEAM_MEMBER();
        for (int i = 0; i < team_member.length; i++) {
            if (team_member[i] != null && team_member[i].equals(user.getUserId())) {
                team_member[i] = null;
                break;
            }
        }

        if (playTeam.getTeamLeaderId().equals(user.getUserId())) {
            // 如果 队长退出队伍,选择新队员成为队长
            team_member = playTeam.getTEAM_MEMBER();
            for (int i = 0; i < team_member.length; i++) {
                if (team_member[i] != null) {
                    playTeam.setTeamLeaderId(team_member[i]);
                    break;
                }
            }
        }

        Duplicate currDuplicate = playTeam.getCurrDuplicate();
        if (currDuplicate != null) {
            // 若当前副本不为空
            BossMonster currBossMonster = currDuplicate.getCurrBossMonster();
            synchronized (currBossMonster.getCHOOSE_USER_MONITOR()) {
                Map<Integer, Integer> userIdMap = currBossMonster.getUserIdMap();
                userIdMap.remove(user.getUserId());
            }
        }

        user.setPlayTeam(null);
        GameMsg.UserInfo.Builder userInfo = GameMsg.UserInfo.newBuilder()
                .setUserId(user.getUserId())
                .setUserName(user.getUserName());
        GameMsg.UserQuitTeamResult userQuitTeamResult = GameMsg.UserQuitTeamResult.newBuilder()
                .setUserInfo(userInfo)
                .setTeamLeaderId(playTeam.getTeamLeaderId())
                .build();
        user.getCtx().writeAndFlush(userQuitTeamResult);
        for (Integer id : team_member) {
            if (id != null) {
                User userById = UserManager.getUserById(id);
                userById.getCtx().writeAndFlush(userQuitTeamResult);
            }
        }
    }


    /**
     * 返回还存活的怪集合
     *
     * @param monsterList 所有怪的集合
     * @return 存活怪的集合
     */
    public List<Monster> getMonsterAliveList(Collection<Monster> monsterList) {
        List<Monster> monsterAliveList = new ArrayList<>();
        //存活的 怪
        for (Monster monster : monsterList) {
            if (!monster.isDie()) {
                monsterAliveList.add(monster);
            }
        }
        return monsterAliveList;
    }

    /**
     * 获取当前副本
     *
     * @param user
     * @return
     */
    public Duplicate getPlayTeam(User user) {
        PlayTeam playTeam = user.getPlayTeam();
        if (playTeam == null) {
            return user.getCurrDuplicate();
        } else {
            return playTeam.getCurrDuplicate();
        }
    }

    /**
     * 取消召唤师定时器
     *
     * @param user
     */
    public void cancelSummonTimerOrPlayTeam(User user) {
        //取消召唤兽, 定时器
        PlayTeam playTeam = user.getPlayTeam();
        if (playTeam == null) {
            cancelSummonTimer(user);
        } else {
            Integer[] team_member = playTeam.getTEAM_MEMBER();
            for (Integer id : team_member) {
                if (id != null) {
                    User userById = UserManager.getUserById(id);
                    cancelSummonTimer(userById);
                }
            }
        }
    }
    public void cancelSummonTimer(User user) {
        Map<Integer, SummonMonster> summonMonsterMap = user.getSummonMonsterMap();
        for (SummonMonster value : summonMonsterMap.values()) {
            if (value != null){
                value.setHp((int) (ProfessionConst.HP * 0.5));
                value.setWeakenDefense(0);
                user.getSummonMonsterRunnableScheduledFutureMap().get(value).cancel(true);
            }
        }
    }


    /**
     *
     * @param userId
     * @param effectTime
     * @return
     */
    public ForceAttackUser createForeAttackUser(int userId, float effectTime) {
        ForceAttackUser forceAttackUser = new ForceAttackUser();
        forceAttackUser.setStartTime(System.currentTimeMillis());
        forceAttackUser.setEndTime(System.currentTimeMillis() + ((long) effectTime * 1000));
        forceAttackUser.setUserId(userId);
        return forceAttackUser;
    }
}
