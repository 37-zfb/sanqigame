package server;

import com.google.protobuf.GeneratedMessageV3;
import constant.*;
import entity.db.CurrUserStateEntity;
import entity.db.UserEquipmentEntity;
import entity.db.UserPotionEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import server.model.duplicate.BossMonster;
import server.model.duplicate.Duplicate;
import server.model.duplicate.ForceAttackUser;
import server.model.profession.SummonMonster;
import server.model.props.AbstractPropsProperty;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import server.model.scene.Monster;
import msg.GameMsg;
import server.scene.GameData;
import server.model.PlayTeam;
import server.model.User;
import server.model.UserManager;
import server.service.UserService;
import server.timer.BossAttackTimer;
import server.timer.MonsterAttakTimer;
import type.DuplicateType;
import type.PropsType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RunnableScheduledFuture;

/**
 * @author 张丰博
 */
@Slf4j
public final class PublicMethod {

    private static final PublicMethod PUBLIC_METHOD = new PublicMethod();

    private final UserService userService = GameServer.APPLICATION_CONTEXT.getBean(UserService.class);

    private PublicMethod() {
    }

    public static PublicMethod getInstance() {
        return PUBLIC_METHOD;
    }

    /**
     * 创建 用户状态对象
     * @param user
     * @return
     */
    public CurrUserStateEntity createUserState(User user) {
        CurrUserStateEntity userStateEntity = new CurrUserStateEntity();
        user.calCurrHp();
        userStateEntity.setCurrHp(user.getCurrHp());
        // 计算当前mp
        user.calCurrMp();
        userStateEntity.setCurrMp(user.getCurrMp());
        userStateEntity.setCurrSceneId(user.getCurSceneId());
        userStateEntity.setBaseDamage(user.getBaseDamage());
        userStateEntity.setBaseDefense(user.getBaseDefense());
        userStateEntity.setUserId(user.getUserId());
        userStateEntity.setMoney(user.getMoney());

        if (user.getPlayGuild()!=null){
            user.getPlayGuild().getGuildMemberMap().get(user.getUserId()).setOnline(false);
            userStateEntity.setGuildId(user.getPlayGuild().getGuildMemberMap().get(user.getUserId()).getGuildId());
        }else {
            userStateEntity.setGuildId(0);
        }
        return userStateEntity;
    }


    /**
     * 用户普通攻击或者带伤害的技能攻击
     *
     * @param user          用户对象
     * @param currDuplicate 当前所在副本
     * @param subHp         减血量
     */
    public void normalOrSkillAttackBoss(User user, Duplicate currDuplicate, Integer subHp, SummonMonster summonMonster) {

        PlayTeam playTeam = user.getPlayTeam();
        BossMonster currBossMonster = currDuplicate.getCurrBossMonster();

        GameMsg.AttkBossResult.Builder newBuilder1 = GameMsg.AttkBossResult.newBuilder();

        synchronized (currBossMonster.getATTACK_BOSS_MONITOR()) {

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
                    sendMsg(user.getCtx(), nextBossResult);

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
                    sendMsg(user.getCtx(), duplicateFinishResult);
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
        sendMsg(user.getCtx(), attkBossResult);
    }

    /**
     * 随机选一个怪攻击
     *
     * @param user          用户对象
     * @param summonMonster 召唤兽
     */
    public void userOrSummonerAttackMonster(User user, Monster monster, SummonMonster summonMonster, Integer subHp) {
        GameMsg.AttkResult.Builder attkResultBuilder = GameMsg.AttkResult.newBuilder();
        // 普通攻击

//        int subHp;
//        if (summonMonster == null) {
//            subHp = user.calMonsterSubHp();
//        } else {
//            subHp = summonMonster.calMonsterSubHp();
//        }

        // 使用当前被攻击的怪对象，做锁对象
        synchronized (monster.getSubHpMonitor()) {
            // 减血  (0~99) + 500
            if (monster.isDie()) {
                log.info("{} 已被其他玩家击杀!", monster.getName());
                // 有可能刚被前一用户杀死，
                GameMsg.DieResult dieResult = GameMsg.DieResult.newBuilder()
                        .setMonsterId(monster.getId())
                        .setIsDieBefore(true)
                        .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                        .build();
                user.getCtx().channel().writeAndFlush(dieResult);
                return;
            } else if (monster.getHp() <= subHp) {
                log.info("玩家:{},击杀:{}!", user.getUserName(), monster.getName());
                monster.setHp(0);
                monster.getRunnableScheduledFuture().cancel(true);

                // 添加奖励
                String propsIdString = monster.getPropsId();
                String[] split = propsIdString.split(",");
                int propsId = Integer.parseInt(split[(int) Math.random() * split.length]);
                user.addMonsterReward(propsId);

                GameMsg.DieResult.Builder dieBuilder = GameMsg.DieResult.newBuilder();
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

                synchronized (monster.getCHOOSE_USER_MONITOR()) {
                    if (summonMonster == null) {
                        Map<Integer, Integer> userIdMap = monster.getUserIdMap();
                        if (!userIdMap.containsKey(user.getUserId())) {
                            userIdMap.put(user.getUserId(), subHp);
                        } else {
                            Integer oldSubHp = userIdMap.get(user.getUserId());
                            userIdMap.put(user.getUserId(), oldSubHp + subHp);
                        }
                        log.info("玩家:{},使:{} 减血 {}!", user.getUserName(), monster.getName(), subHp);
                    } else {
                        // 召唤兽
                        Map<SummonMonster, Integer> summonMonsterMap = monster.getSummonMonsterMap();
                        if (!summonMonsterMap.containsKey(summonMonster)) {
                            summonMonsterMap.put(summonMonster, subHp);
                        } else {
                            Integer oldSubHp = summonMonsterMap.get(summonMonster);
                            summonMonsterMap.put(summonMonster, oldSubHp + subHp);
                        }
                        log.info("召唤兽,使:{} 减血 {}!", monster.getName(), subHp);
                    }
                }

                if (monster.getRunnableScheduledFuture() == null) {
                    // 定时器为null,设置boss定时器， 攻击玩家
                    MonsterAttakTimer.getInstance().monsterNormalAttk(monster);
                }
            }
        }

        // 通知客户端 xx怪 减血；
        attkResultBuilder.setMonsterId(monster.getId())
                .setSubHp(subHp);
        // 怪减血，广播通知当前场景所有用户
        GameMsg.AttkResult attkResult = attkResultBuilder.build();
        Broadcast.broadcast(user.getCurSceneId(), attkResult);
    }

    public void subMonsterHp() {

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
     * 发送数据,当组队状态时，放给队伍成员
     *
     * @param ctx
     * @param msg
     */
    public void sendMsg(ChannelHandlerContext ctx, GeneratedMessageV3 msg) {
        User user = getUser(ctx);
        PlayTeam playTeam = user.getPlayTeam();
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
        Potion po = null;
        if (!isExist) {
            if (backpack.size() >= BackPackConst.MAX_CAPACITY) {
                // 此时背包已满，
                throw new CustomizeException(CustomizeErrorCode.BACKPACK_SPACE_INSUFFICIENT);
            }

            userPotionEntity.setNumber(number);

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
//        if (potionId != null) {
//            userPotionEntity.setId(potionId);
        userPotionEntity.setId(userPotionEntity.getId());
//        }
        if (po != null) {
            po.setId(userPotionEntity.getId());
        }
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
        synchronized (playTeam.getTEAM_MONITOR()) {
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

    /**
     * 取消指定用户，召唤兽的所有定时器
     *
     * @param user
     */
    public void cancelSummonTimer(User user) {
        Map<Integer, SummonMonster> summonMonsterMap = user.getSummonMonsterMap();
        for (SummonMonster value : summonMonsterMap.values()) {
            if (value != null) {
                value.setHp((int) (ProfessionConst.HP * 0.5));
                value.setWeakenDefense(0);
                while (!user.getSummonMonsterRunnableScheduledFutureMap().get(value).cancel(true)) {
                }
            }
        }
    }


    /**
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

    /**
     * 取消怪对当前用户的攻击
     *
     * @param user 当前用户
     */
    public void cancelMonsterAttack(User user) {
        // 取消当前场景所有 怪 对当前用户的攻击
        // 当前场景所有的怪
        Map<Integer, Monster> monsterMap = GameData.getInstance().getSceneMap().get(user.getCurSceneId()).getMonsterMap();
        if (monsterMap.size() != 0) {
            for (Monster monster : monsterMap.values()) {
                monster.getUserIdMap().remove(user.getUserId());
            }
        }
        // 取消召唤兽定时器
        Map<SummonMonster, RunnableScheduledFuture<?>> scheduledFutureMap = user.getSummonMonsterRunnableScheduledFutureMap();
        cancelSummonTimer(user);
        for (SummonMonster value : scheduledFutureMap.keySet()) {
            scheduledFutureMap.remove(value);
        }

    }


}
