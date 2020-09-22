package server.model;

import constant.BossMonsterConst;
import constant.ProfessionConst;
import entity.MailProps;
import entity.db.UserEquipmentEntity;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import server.PublicMethod;
import server.cmdhandler.duplicate.BossSkillAttack;
import server.timer.logout.LogoutTimer;
import server.util.PropsUtil;
import server.cmdhandler.mail.MailUtil;
import server.model.duplicate.BossMonster;
import server.model.duplicate.Duplicate;
import server.model.profession.Skill;
import server.model.profession.SummonMonster;
import server.model.props.AbstractPropsProperty;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import server.scene.GameData;
import type.EquipmentType;
import type.PotionType;
import type.PropsType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;

/**
 * @author 张丰博
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * 用户 Id
     */
    private int userId;

    /**
     * 当前等级
     */
    private Integer lv;
    /**
     * 当前经验
     */
    private long experience;


    /**
     * 职业id
     */
    private int professionId;

    /**
     * 用户名称
     */
    private String userName;


    /**
     * 当前血量
     */
    private int currHp;

    /**
     * 当前蓝量
     */
    private volatile int currMp;

    /**
     * 当前场景
     */
    private Integer curSceneId;

    /**
     * 已死亡
     */
    private boolean died;

    /**
     * 拥有的技能集合  技能id <==> 技能
     */
    private final Map<Integer, Skill> skillMap = new HashMap<>();
    /**
     * 释放技能时的吟唱状态
     */
    private RunnableScheduledFuture<?> isPrepare;
    /**
     * 召唤兽集合
     * 召唤师技能id <==> 召唤兽
     */
    private final Map<Integer, SummonMonster> summonMonsterMap = new ConcurrentHashMap<>();
    private final Map<SummonMonster, RunnableScheduledFuture<?>> summonMonsterRunnableScheduledFutureMap = new ConcurrentHashMap<>();


    /**
     * 基础伤害
     */
    private int baseDamage;

    /**
     * 基础防御
     */
    private int baseDefense;
    /**
     * 附加属性
     */
    private volatile int weakenDefense = 0;


    /**
     * 装备栏
     */
    private final UserEquipmentEntity[] userEquipmentArr = new UserEquipmentEntity[9];

    /**
     * 当前金币
     */
    private int money;

    /**
     * 背包
     */
    private final Map<Integer, Props> backpack = new HashMap<>();

    /**
     * 当前所在副本
     */
    private Duplicate currDuplicate;

    /**
     * 管道上下文
     */
    private ChannelHandlerContext ctx;

    /**
     * 记录恢复状态
     */
    private final UserResumeState userResumeState = new UserResumeState();

    /**
     * mp监视器
     */
    private final Object mpMonitor = new Object();

    /**
     * hp 监视器
     */
    private final Object hpMonitor = new Object();

    /**
     * mp自动回复任务
     */
    private ScheduledFuture<?> mpTask;


    /**
     * 掉血自动回复任务
     */
    private ScheduledFuture<?> subHpTask;
    private int subHpNumber = 0;


    /**
     * 限购商品，允许购买个数; 商品id <==> 允许购买个数
     */
    private final Map<Integer, Integer> goodsAllowNumber = new HashMap<>();


    /**
     * 邮件系统
     */
    private final PlayMail mail = new PlayMail();

    /**
     * 竞技场
     */
    private PlayArena playArena;


    /**
     * 队伍系统
     */
    private volatile PlayTeam playTeam;

    /**
     * 邀请组队人id
     */
    private final Map<Integer, Long> invitationUserIdMap = new ConcurrentHashMap<>();

    /**
     * 组队监听器
     */
    private final Object TEAM_MONITOR = new Object();


    /**
     * 交易系统
     */
    private final PlayDeal PLAY_DEAL = new PlayDeal();

    /**
     * 公会系统
     */
    private PlayGuild playGuild = null;

    /**
     * 任务系统
     */
    private final PlayTask playTask = new PlayTask();

    /**
     * 朋友
     */
    private final PlayFriend PLAY_FRIEND = new PlayFriend();

    /**
     * 注销定时器
     */
    private ScheduledFuture<?> logoutTimer;


    /**
     * 护盾
     */
    private Integer shieldValue = 0;
    private final Object SHIELD_MONITOR = new Object();

    /**
     * 注销锁
     */
    private final Object LOGOUT_MONITOR = new Object();

    /**
     * 设置恢复mp终止时间
     */
    public void resumeMpTime() {
        if (currMp < ProfessionConst.MP) {
            // 记录距离满血差值
            int value = ProfessionConst.MP - currMp;

            // 下面一段，计算当前是否有缓慢药剂正在恢复中，若有则要计算当前还剩下多少药量；
            Potion potion = getPotion(PotionType.MP);
            if (potion != null) {
                Long endTimeMp = potion.getUsedEndTime();
                if (endTimeMp != 0 && endTimeMp >= System.currentTimeMillis()) {
                    // 此时药效未使用完
                    int v = (int) ((endTimeMp - System.currentTimeMillis()) / 4000) * 400;
                    value -= v;
                }
            }

            // 设置恢复结束时间,
            this.userResumeState.setEndTimeMp(System.currentTimeMillis() + value * 1000);
            // 起始时间
            this.userResumeState.setStartTimeMp(System.currentTimeMillis());
        } else {
            this.userResumeState.setEndTimeMp(0L);
        }
    }

    public void resumeMpPotionTime() {
        if (currMp < ProfessionConst.MP) {
            // 当前蓝量差值
            int value = ProfessionConst.MP - currMp;
            // 设置恢复结束时间
            this.userResumeState.setEndTimeMp(System.currentTimeMillis() + value * 1000 - 400 * 1000);
            // 起始时间
            this.userResumeState.setStartTimeMp(System.currentTimeMillis());
        } else {
            this.userResumeState.setEndTimeMp(0L);
        }
    }


    /**
     * 计算怪减血量
     * 当穿装备时，会加上装备的伤害加成
     *
     * @return 血量
     */
    public Integer calMonsterSubHp() {

        int equDamage = getEquDamage()*10;
        int subHp = (int) ((Math.random() * this.getBaseDamage()) + 800 + 3 * equDamage);

        log.info("玩家: {},伤害: {}", this.getUserName(), subHp);
//        subHp = 2000;
        return subHp;
    }

    /**
     * 获得装备伤害加成
     *
     * @return
     */
    public int getEquDamage() {
        int equDamage = 0;
        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        for (int i = 0; i < this.userEquipmentArr.length; i++) {
            if (this.userEquipmentArr[i] != null) {
                Props props = propsMap.get(this.userEquipmentArr[i].getPropsId());
                Equipment propsProperty = (Equipment) props.getPropsProperty();
                if (propsProperty.getEquipmentType() == EquipmentType.Weapon) {
                    // 武器
                    if (propsProperty.getDurability() <= 0) {
                        // 此时耐久度不够,给客户端发消息
                        log.info("用户: {}, 武器: {}, 耐久度: {}", this.getUserName(), props.getName(), userEquipmentArr[i].getDurability());
                        GameMsg.DurabilityDeficiencyResult durabilityDeficiencyResult = GameMsg.DurabilityDeficiencyResult.newBuilder()
                                .setUserEquipmentId(userEquipmentArr[i].getId())
                                .build();
                        ctx.channel().writeAndFlush(durabilityDeficiencyResult);
                    } else {
                        //耐久度-1
                        log.info("当前 {} 耐久度:{} -1", props.getName(), this.userEquipmentArr[i].getDurability());
                        this.userEquipmentArr[i].setDurability(this.userEquipmentArr[i].getDurability() - 1);
                        equDamage += propsProperty.getDamage();
                    }
                } else {
                    // 其他装备
                    equDamage += propsProperty.getDamage();
                }
            }
        }
        return equDamage;
    }

    /**
     * 减蓝并且重新设置恢复终止时间
     *
     * @param consumeMp
     */
    public void subMp(Integer consumeMp) {
        // 使用技能后的血量
        this.currMp = currMp - consumeMp;
        //重新设置终止时间
        int mp = ProfessionConst.MP - this.currMp;
        this.getUserResumeState().setStartTimeMp(System.currentTimeMillis());
        this.getUserResumeState().setEndTimeMp(System.currentTimeMillis() + mp * 1000);
    }

    /**
     * 计算当前mp
     */
    public void calCurrMp() {
        synchronized (this.mpMonitor) {
            // 自动恢复mp
            Long userEndTimeMp = this.getUserResumeState().getEndTimeMp();
            if (userEndTimeMp >= System.currentTimeMillis()) {
                // 此时在恢复中
                int v = (int) (System.currentTimeMillis() - this.userResumeState.getStartTimeMp()) / 1000;
                currMp = currMp + v;
            } else {
                // 此时已经恢复完了
                currMp = ProfessionConst.MP;
            }


            Potion potion = getPotion(PotionType.MP);
            if (potion == null) {
                return;
            }
            if (currMp >= ProfessionConst.MP) {
                potion.setUsedEndTime(0L);
                potion.setUsedStartTime(0L);
            } else {
                Long potionEndTimeMp = potion.getUsedEndTime();
                if (potionEndTimeMp != 0 && potionEndTimeMp >= System.currentTimeMillis()) {
                    // 此时药效未使用完
                    long startTime = potion.getUsedStartTime();
                    long currentTime = System.currentTimeMillis();
                    float v = (float) (currentTime - startTime) / 4000 * 400;
                    currMp += v;
                    potion.setUsedStartTime(System.currentTimeMillis());
                } else if (potionEndTimeMp != 0) {
                    // 此时药效使用完
                    float v = (float) (potionEndTimeMp - potion.getUsedStartTime()) / 4000 * 400;
                    if (ProfessionConst.MP <= (currMp + v)) {
                        currMp = ProfessionConst.MP;
                    } else {
                        currMp += v;
                    }
                    potion.setUsedEndTime(0L);
                    potion.setUsedStartTime(0L);
                }
            }


        }

    }

    /**
     * 计算当前血量,并重新设计时间
     */
    public void calCurrHp() {
        synchronized (this.hpMonitor) {
            Potion potion = getPotion(PotionType.HP);
            if (potion == null) {
                return;
            }
            if (currHp >= ProfessionConst.HP) {
                potion.setUsedEndTime(0L);
                potion.setUsedStartTime(0L);
                return;
            }

            Long endTimeHp = potion.getUsedEndTime();
            long currentTimeMillis = System.currentTimeMillis();
            if (endTimeHp != 0 && endTimeHp >= currentTimeMillis) {
                // 此时药效未使用完
                long startTime = potion.getUsedStartTime();
                float v = (float) (currentTimeMillis - startTime) / 4000 * 400;
                currHp += v;
                potion.setUsedStartTime(currentTimeMillis);
                log.info("用户 {} 加血 {} 当前血量 {};", userName, v, currHp);
            } else if (endTimeHp != 0) {
                // 此时药效使用完
                float v = (float) (endTimeHp - potion.getUsedStartTime()) / 4000 * 400;
                if (ProfessionConst.HP <= (currHp + v)) {
                    currHp = ProfessionConst.HP;
                } else {
                    currHp += v;
                }
                log.info("用户 {} 加血 {} 当前血量 {};", userName, v, currHp);
                potion.setUsedEndTime(0L);
                potion.setUsedStartTime(0L);
            }
        }
    }

    /**
     * 计算目标用户减血量
     *
     * @param defense 目标用户防御
     */
    public int calTargetUserSubHp(Integer defense) {
        Integer subHp = calMonsterSubHp();
        return subHp - defense * 2;
    }


    /**
     * 根据 缓慢恢复药剂类型(MP、HP)获得药剂
     *
     * @return
     */
    private Potion getPotion(PotionType type) {
        for (Props value : backpack.values()) {
            AbstractPropsProperty propsProperty = value.getPropsProperty();
            if (propsProperty.getType() == PropsType.Potion
                    && ((Potion) propsProperty).getInfo().contains(PotionType.SLOW.getType())
                    && ((Potion) propsProperty).getInfo().contains(type.getType())) {
                return (Potion) propsProperty;
            }
        }
        return null;
    }

    /**
     * boss攻击用户，用户减血
     *
     * @param bossMonster
     * @param subHp
     */
    public void bossAttackSubHp(BossMonster bossMonster, Integer subHp) {
        if (bossMonster.getOrdinaryAttack() > BossMonsterConst.ORDINARY_ATTACK) {
            //十秒后，防御属性回归正常
            this.setWeakenDefense(0);
            bossMonster.setOrdinaryAttack(0);
            // 每五次普通攻击，一次技能攻击
            BossSkillAttack.getInstance().bossSkillAttack(this, bossMonster, null);
        }else {
            synchronized (this.getHpMonitor()) {
                // 用户减血
                if (this.getCurrHp() <= 0 || (this.getCurrHp() + this.shieldValue - subHp) <= 0) {
                    log.info("用户: {} 已死亡;", this.getUserName());
                    synchronized (SHIELD_MONITOR){
                        this.shieldValue = 0;
                    }
                    this.setCurrHp(0);
                    // boss打死了玩家;

                    PublicMethod.getInstance().cancelSummonTimer(this);

                    GameMsg.BossKillUserResult bossKillUserResult = GameMsg.BossKillUserResult.newBuilder()
                            .setTargetUserId(this.getUserId())
                            .build();
                    this.getCtx().writeAndFlush(bossKillUserResult);
                } else {
                    log.info("用户: {} , 当前血量: {} , 受到伤害减血: {}", this.getUserName(), this.getCurrHp(), subHp);
                    // 普通攻击数 加一;
                    bossMonster.setOrdinaryAttack(bossMonster.getOrdinaryAttack() + 1);
                    // 用户减血
                    synchronized (SHIELD_MONITOR){
                        if (this.shieldValue > subHp) {
                            this.shieldValue -= subHp;
                            log.info("用户 {} 护盾减少 {} 剩余 {};", userName,subHp,shieldValue);
                            PublicMethod.getInstance().sendShieldMsg(subHp, this);
                        } else if (this.shieldValue > 0 && this.shieldValue < subHp) {
                            subHp -= this.shieldValue;
                            log.info("用户 {} 护盾减少 {} 剩余 {};", userName,shieldValue,0);
                            PublicMethod.getInstance().sendShieldMsg(shieldValue, this);

                            this.shieldValue = 0;
                            this.setCurrHp(this.getCurrHp() - subHp);
                        } else {
                            this.setCurrHp(this.getCurrHp() - subHp);
                        }
                    }
                    GameMsg.BossAttkUserResult attkCmd = GameMsg.BossAttkUserResult.newBuilder()
                            .setSubUserHp(subHp)
                            .build();
                    this.getCtx().writeAndFlush(attkCmd);
                }
            }
        }

    }

    /**
     * 怪攻击，减血
     *
     * @param monsterName 怪对象
     * @param subHp       减血量
     */
    public void monsterAttackSubHp(String monsterName, Integer subHp) {
        synchronized (this.getHpMonitor()) {
            // 用户减血
            if (this.getCurrHp() <= 0 || (this.getCurrHp()+ this.shieldValue - subHp) <= 0) {
                this.setCurrHp(0);
                synchronized (SHIELD_MONITOR){
                    this.shieldValue = 0;
                }

                PublicMethod.getInstance().cancelMonsterAttack(this);
                // 发送死亡消息
                GameMsg.DieResult dieResult = GameMsg.DieResult.newBuilder()
                        .setTargetUserId(this.getUserId())
                        .build();
                ctx.channel().writeAndFlush(dieResult);
            } else {

                synchronized (SHIELD_MONITOR){
                    if (this.shieldValue > subHp) {
                        this.shieldValue -= subHp;
                        log.info("用户 {} 护盾减少 {} 剩余 {};", userName,subHp,shieldValue);
                        PublicMethod.getInstance().sendShieldMsg(subHp,this);
                    } else if (this.shieldValue > 0 && this.shieldValue < subHp) {
                        subHp -= this.shieldValue;
                        log.info("用户 {} 护盾减少 {} 剩余 {};", userName,this.shieldValue,0);
                        PublicMethod.getInstance().sendShieldMsg(shieldValue,this);

                        this.shieldValue = 0;
                        this.setCurrHp(this.getCurrHp() - subHp);
                    } else {
                        this.setCurrHp(this.getCurrHp() - subHp);
                    }
                }
                GameMsg.AttkCmd attkCmd = GameMsg.AttkCmd.newBuilder()
                        .setTargetUserId(this.getUserId())
                        .setMonsterName(monsterName)
                        .setSubHp(subHp)
                        .build();
                ctx.channel().writeAndFlush(attkCmd);
            }
        }
    }

    /**
     * 添加 怪 爆出的奖励
     *
     * @param propsId 奖励的道具id
     */
    public void addMonsterReward(Integer propsId) {
        Props props = GameData.getInstance().getPropsMap().get(propsId);

        try {
            if (props.getPropsProperty().getType() == PropsType.Equipment) {
                PropsUtil.getPropsUtil().addEquipment(this, props, null);
            } else if (props.getPropsProperty().getType() == PropsType.Potion) {
                PropsUtil.getPropsUtil().addPotion(props, this, 1, null);
            }
        } catch (CustomizeException e) {
            log.error(e.getMessage(), e);
        }

    }


    /**
     * 添加道具
     *
     * @param props
     * @param number
     */
    public void addProps(Props props, int number) {
        PublicMethod publicMethod = PublicMethod.getInstance();
        if (props.getPropsProperty().getType() == PropsType.Equipment) {
            publicMethod.addEquipment(this, props);
        } else if (props.getPropsProperty().getType() == PropsType.Potion) {
            publicMethod.addPotion(props, this, number);
        }
    }


}
