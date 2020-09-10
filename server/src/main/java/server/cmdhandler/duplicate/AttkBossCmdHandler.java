package server.cmdhandler.duplicate;

import constant.BossMonsterConst;
import constant.DuplicateConst;
import entity.db.UserEquipmentEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
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
import server.scene.GameData;
import server.service.UserService;
import server.timer.BossAttackTimer;
import server.timer.state.DbUserStateTimer;
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
    private DbUserStateTimer userStateTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.AttkBossCmd attkBossCmd) {
        MyUtil.checkIsNull(ctx, attkBossCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        Duplicate currDuplicate = PublicMethod.getInstance().getDuplicate(user);
        if (currDuplicate == null){
            return;
        }

        BossMonster currBossMonster = currDuplicate.getCurrBossMonster();
        if (currBossMonster == null){
            return;
        }

        Integer subHp = user.calMonsterSubHp();

        if ((currBossMonster.getEnterRoomTime() + DuplicateConst.BOSS_TIME) < System.currentTimeMillis()) {
            // 副本超时
            log.error("用户: {} , 副本: {} , boss: {} , 超时;", user.getUserName(), currDuplicate.getName(), currBossMonster.getBossName());

            BossAttackTimer.getInstance().cancelTask(currDuplicate.getCurrBossMonster().getScheduledFuture());
            PublicMethod.getInstance().cancelSummonTimerOrPlayTeam(user);

            //持久化装备耐久度
            PublicMethod.getInstance().dbWeaponDurability(user.getUserEquipmentArr());

            user.setCurrDuplicate(null);
            // 用户退出
            throw new CustomizeException(CustomizeErrorCode.DUPLICATE_TIME_OUT);
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

                    GameMsg.BossAllKillResult bossAllKillResult = GameMsg.BossAllKillResult.newBuilder().build();
                    PublicMethod.getInstance().sendMsg(ctx, bossAllKillResult);
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
