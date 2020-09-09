package server.cmdhandler.duplicate;

import constant.DuplicateConst;
import constant.ProfessionConst;
import entity.db.CurrUserStateEntity;
import entity.db.UserEquipmentEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import server.model.duplicate.Duplicate;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayTeam;
import server.model.User;
import server.model.props.Equipment;
import server.model.props.Props;
import server.scene.GameData;
import server.timer.BossAttackTimer;
import server.timer.state.DbUserStateTimer;
import type.EquipmentType;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 * 退出副本
 */
@Component
@Slf4j
public class UserQuitDuplicateCmdHandler implements ICmdHandler<GameMsg.UserQuitDuplicateCmd> {

    @Autowired
    private DbUserStateTimer userStateTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserQuitDuplicateCmd userQuitDuplicateCmd) {

        MyUtil.checkIsNull(ctx, userQuitDuplicateCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        Duplicate currDuplicate;
        PlayTeam playTeam = user.getPlayTeam();
        if (playTeam == null) {
            currDuplicate = user.getCurrDuplicate();
            user.setCurrDuplicate(null);
        } else {
            currDuplicate = playTeam.getCurrDuplicate();
            playTeam.setCurrDuplicate(null);
        }
        if (currDuplicate != null) {
            // 取消定时器
            BossAttackTimer.getInstance().cancelTask(currDuplicate.getCurrBossMonster().getScheduledFuture());
        }

        /**
         *  取消召唤师定时器
         */
        PublicMethod.getInstance().cancelSummonTimerOrPlayTeam(user);

        user.setCurrHp(ProfessionConst.HP);
        user.setCurrMp(ProfessionConst.MP);

        //持久化装备耐久度
        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        for (UserEquipmentEntity equipmentEntity : user.getUserEquipmentArr()) {
            if (equipmentEntity != null) {
                if (((Equipment) propsMap.get(equipmentEntity.getPropsId()).getPropsProperty()).getEquipmentType() == EquipmentType.Weapon) {
                    //如果是武器
                    userStateTimer.modifyUserEquipment(equipmentEntity);
                }
            }

        }

        GameMsg.UserQuitDuplicateResult.Builder newBuilder = GameMsg.UserQuitDuplicateResult.newBuilder();
        if (user.getCurrHp() <= 0) {
            newBuilder.setQuitDuplicateType(DuplicateConst.USER_KILLED);
        } else {
            newBuilder.setQuitDuplicateType(DuplicateConst.USER_NORMAL_QUIT_DUPLICATE);
        }

        // 用户退出
        GameMsg.UserQuitDuplicateResult userQuitDuplicateResult = newBuilder.build();

        ctx.writeAndFlush(userQuitDuplicateResult);

    }
}
