package client.model;

import client.model.arena.PlayArenaClient;
import client.model.deal.PlayDealClient;
import client.model.guild.PlayGuildClient;
import client.model.server.duplicate.Duplicate;
import client.model.server.profession.Skill;
import client.model.server.props.Equipment;
import client.model.server.props.Potion;
import client.model.server.props.Props;
import client.model.task.PlayTaskClient;
import client.model.team.PlayTeamClient;
import client.scene.GameData;
import client.timer.ResumeStateTimer;
import constant.ProfessionConst;
import entity.db.UserEquipmentEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import type.EquipmentType;
import type.PotionType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * @author 张丰博
 */
@Data
@AllArgsConstructor
public class Role {

    /**
     * id
     */
    private Integer id;

    /**
     * 职业id
     */
    private Integer professionId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 当前血量
     */
    private volatile Integer currHp;

    /**
     * 当前 蓝量
     */
    private Integer currMp;

    /**
     * 当前场景id
     */
    private Integer currSceneId;

    /**
     * 当前所在副本
     */
    private Duplicate currDuplicate;

    /**
     * mp定时器
     */
    private final Object mpMonitor = new Object();

    /**
     * hp定时器
     */
    private final Object hpMonitor = new Object();

    /**
     * 恢复状态
     */
    private final UserResumeState userResumeState = new UserResumeState();

    /**
     * 拥有的技能集合   id 技能
     */
    private final Map<Integer, Skill> skillMap = new HashMap<>();


    /**
     * 背包
     */
    private final Map<Integer, Props> backpackClient = new HashMap<>();

    /**
     * 邮件
     */
    private final MailClient mail = new MailClient();

    /**
     * 金币
     */
    private int money;

    /**
     * 装备栏
     */
    private final UserEquipmentEntity[] userEquipmentEntityArr = new UserEquipmentEntity[9];

    /**
     * mp自动回复任务
     */
    private ScheduledFuture<?> mpTask;
    /**
     * mp自动回复任务
     */
    private ScheduledFuture<?> hpTask;


    /**
     * 限购商品，允许购买的个数
     */
    private final Map<Integer, Integer> GOODS_ALLOW_NUMBER = new HashMap<>();

    /**
     * 竞技场
     */
    private final PlayArenaClient ARENA_CLIENT = new PlayArenaClient();

    /**
     * 组队系统
     */
    private final PlayTeamClient TEAM_CLIENT = new PlayTeamClient();

    /**
     *  交易系统
     */
    private final PlayDealClient DEAL_CLIENT = new PlayDealClient();

    private boolean isChat = false;

    private boolean isSelf = false;

    private boolean isTeam = false;

    private boolean isAnswer = false;

    private boolean isDeal = false;

    private boolean isAppoint = false;

    private boolean isEliminate = false;

    private boolean isTransferPresident = false;

    /**
     *  公会系统
     */
    private PlayGuildClient playGuildClient;


    /**
     * 任务
     */
    private final PlayTaskClient playTaskClient = new PlayTaskClient();

    private static final Role role = new Role();

    private Role() {
    }

    public static Role getInstance() {
        return role;
    }

    public void startResumeMp() {
        if (currMp < ProfessionConst.MP && this.getUserResumeState().getEndTimeMp() > System.currentTimeMillis()) {
            // 此时需要恢复mp状态,并且定时器没有启动
            if (mpTask == null) {
                ResumeStateTimer.getInstance().resumeStateAutomatic(role);
            }
        }
    }

    /**
     * 使用 药剂 缓慢恢复
     */
    public void startPotionResumeState(Potion potion) {
        if (potion.getInfo().contains(PotionType.HP.getType())) {
            ResumeStateTimer.getInstance().resumeStatePotionHp(this, potion);
        } else if (potion.getInfo().contains(PotionType.MP.getType())) {
            ResumeStateTimer.getInstance().resumeStatePotionMp(this, potion);
        }
    }


    public void decreaseDurability() {
        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        for (UserEquipmentEntity userEquipmentEntity : userEquipmentEntityArr) {
            if (userEquipmentEntity == null) {
                continue;
            }
            Props props = propsMap.get(userEquipmentEntity.getPropsId());
            if (((Equipment) props.getPropsProperty()).getEquipmentType() == EquipmentType.Weapon) {
                // 持久化武器耐久度
                userEquipmentEntity.setDurability(userEquipmentEntity.getDurability() - 1);
                break;
            }
        }
    }

}
