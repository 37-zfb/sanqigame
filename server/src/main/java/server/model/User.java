package server.model;

import constant.PotionConst;
import constant.ProfessionConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import entity.db.UserEquipmentEntity;
import model.GoodsLimitNumber;
import model.UserResumeState;
import model.duplicate.Duplicate;
import model.profession.Profession;
import model.profession.Skill;
import model.props.AbstractPropsProperty;
import model.props.Equipment;
import model.props.Potion;
import model.props.Props;
import msg.GameMsg;
import scene.GameData;
import type.EquipmentType;
import type.PotionType;
import type.PropsType;

import java.util.HashMap;
import java.util.Map;
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
    private int currMp;

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
     *  限购商品，允许购买个数; 商品id <==> 允许购买个数
     */
    private final Map<Integer,Integer> goodsAllowNumber = new HashMap<>();

    /**
     * 设置恢复mp终止时间
     */
    public void resumeMpTime() {
        int mpGross = ProfessionConst.MP;
        if (currMp < mpGross) {
            // 记录当前时间
            int value = mpGross - currMp;
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
        log.info("玩家: {}, 装备伤害加成: {}", this.getUserName(), 10 * equDamage);

        int subHp = (int) ((Math.random() * this.getBaseDamage()) + 500 + 10 * equDamage);

        log.info("玩家: {},伤害: {}", this.getUserName(), subHp);
        return subHp;
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

            if (potion == null){
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
            if (potion == null ){
                return;
            }
            if ( currHp >= ProfessionConst.HP) {
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
     * @return
     */
    private Potion getPotion(PotionType type) {
        for (Props value : backpack.values()) {
            AbstractPropsProperty propsProperty = value.getPropsProperty();
            if (propsProperty.getType() == PropsType.Potion && ((Potion) propsProperty).getInfo().contains(PotionType.SLOW.getType())
                    && ((Potion) propsProperty).getInfo().contains(type.getType())) {
                return (Potion) propsProperty;
            }
        }
        return null;
    }


}
