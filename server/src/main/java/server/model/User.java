package server.model;

import constant.BossMonsterConst;
import constant.ProfessionConst;
import entity.db.UserEquipmentEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import server.PublicMethod;
import server.cmdhandler.duplicate.BossSkillAttack;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
     * 释放在吟唱状态
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
    private int weakenDefense = 0;


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
     *  邀请人id
     */
    private final Set<Integer> invitationUserId = new HashSet<>();
    /**
     * 组队监听器
     */
    private final Object TEAM_MONITOR = new Object();


    /**
     * 设置恢复mp终止时间
     */
    public void resumeMpTime() {
        int mpGross = ProfessionConst.MP;
        if (currMp < mpGross) {
            // 记录当前时间
            int value = mpGross - currMp;

            // 下面一段，计算当前是否有缓慢药剂正在恢复中，若有则要计算当前还剩下多少药量；
            Potion potion = getPotion(PotionType.MP);
            if (potion == null) {
                return;
            }
            Long endTimeMp = potion.getUsedEndTime();
            if (endTimeMp != 0 && endTimeMp >= System.currentTimeMillis()) {
                // 此时药效未使用完
                int v = (int) ((endTimeMp - System.currentTimeMillis()) / 4000) * 400;
                value -= v;
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

        int equDamage = getEquDamage();
        log.info("玩家: {}, 装备伤害加成: {}", this.getUserName(), 10 * equDamage);

        int subHp = (int) ((Math.random() * this.getBaseDamage()) + 500 + 10 * equDamage);

        log.info("玩家: {},伤害: {}", this.getUserName(), subHp);
        return subHp;
    }

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
                                .setPropsId(userEquipmentArr[i].getPropsId())
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
        int mp = 1000 - this.currMp;
        this.getUserResumeState().setStartTimeMp(System.currentTimeMillis());
        this.getUserResumeState().setEndTimeMp(System.currentTimeMillis() + mp * 1000);
    }

    /**
     * 计算当前mp
     */
    public void calCurrMp() {
        synchronized (this.mpMonitor) {
            Potion potion = getPotion(PotionType.MP);

            if (potion == null) {
                return;
            }
            if (currMp >= ProfessionConst.MP) {
                potion.setUsedEndTime(0L);
                potion.setUsedStartTime(0L);
            } else {
                Long endTimeMp = potion.getUsedEndTime();
                if (endTimeMp != 0 && endTimeMp >= System.currentTimeMillis()) {
                    // 此时药效未使用完
                    long startTime = potion.getUsedStartTime();
                    int v = (int) ((System.currentTimeMillis() - startTime) / 4000) * 400;
                    currMp += v;
                    potion.setUsedStartTime(System.currentTimeMillis());
                } else if (endTimeMp != 0) {
                    // 此时药效使用完
                    int v = (int) ((endTimeMp - potion.getUsedStartTime()) / 4000) * 400;
                    if (ProfessionConst.MP <= (currMp + v)) {
                        currMp = ProfessionConst.MP;
                    } else {
                        currMp += v;
                    }
                    potion.setUsedEndTime(0L);
                    potion.setUsedStartTime(0L);
                }
            }

            // 自动恢复mp
            Long endTimeMp = this.getUserResumeState().getEndTimeMp();
            if (endTimeMp >= System.currentTimeMillis()) {
                // 此时在恢复中
                int v = (int) (System.currentTimeMillis() - this.userResumeState.getStartTimeMp()) / 1000;
                currMp = currMp + v;
            } else {
                // 此时已经恢复完了
                currMp = ProfessionConst.MP;
            }
        }

    }

    /**
     * 计算当前血量
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
            if (endTimeHp != 0 && endTimeHp >= System.currentTimeMillis()) {
                // 此时药效未使用完
                long startTime = potion.getUsedStartTime();
                int v = (int) ((System.currentTimeMillis() - startTime) / 4000) * 400;
                currHp += v;
                potion.setUsedStartTime(System.currentTimeMillis());
            } else if (endTimeHp != 0) {
                // 此时药效使用完
                int v = (int) ((endTimeHp - potion.getUsedStartTime()) / 4000) * 400;
                if (ProfessionConst.HP <= (currHp + v)) {
                    currHp = ProfessionConst.HP;
                } else {
                    currHp += v;
                }
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
        synchronized (this.getHpMonitor()) {
            if (bossMonster.getOrdinaryAttack() > BossMonsterConst.ORDINARY_ATTACK) {
                //十秒后，防御属性回归正常
                this.setWeakenDefense(0);
                bossMonster.setOrdinaryAttack(0);
                // 每五次普通攻击，一次技能攻击
                BossSkillAttack.getInstance().bossSkillAttack(this, bossMonster, null);
            }

            // 用户减血
            if (this.getCurrHp() <= 0 || (this.getCurrHp() - subHp) <= 0) {
                log.info("用户: {} 已死亡;", this.getUserName());
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
                this.setCurrHp(this.getCurrHp() - subHp);
                GameMsg.BossAttkUserResult attkCmd = GameMsg.BossAttkUserResult.newBuilder()
                        .setSubUserHp(subHp)
                        .build();
                this.getCtx().writeAndFlush(attkCmd);
            }
        }
    }

    /**
     *  怪攻击，减血
     * @param monsterName 怪对象
     * @param subHp 减血量
     */
    public void monsterAttackSubHp(String monsterName,Integer subHp) {
        synchronized (this.getHpMonitor()) {
            // 用户减血
            if (this.getCurrHp() <= 0 || (this.getCurrHp() - subHp) <= 0) {
                this.setCurrHp(0);
                PublicMethod.getInstance().cancelMonsterAttack(this);
                // 发送死亡消息
                GameMsg.DieResult dieResult = GameMsg.DieResult.newBuilder()
                        .setTargetUserId(this.getUserId())
                        .build();
                ctx.channel().writeAndFlush(dieResult);
            } else {
                this.setCurrHp(this.getCurrHp() - subHp);
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
     *  添加 怪 爆出的奖励
     * @param propsId 奖励的道具id
     */
    public void addMonsterReward(Integer propsId){
        Props props = GameData.getInstance().getPropsMap().get(propsId);
        if (props.getPropsProperty().getType() == PropsType.Equipment){
            PublicMethod.getInstance().addEquipment(this,props);
        }else if (props.getPropsProperty().getType() == PropsType.Potion){
            PublicMethod.getInstance().addPotion(props,this,1);

        }
    }



}
