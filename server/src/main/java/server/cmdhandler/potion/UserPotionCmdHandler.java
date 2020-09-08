package server.cmdhandler.potion;

import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import server.PublicMethod;
import server.model.props.Props;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import entity.db.UserPotionEntity;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.UserManager;
import server.model.props.Potion;
import server.service.UserService;
import server.timer.state.DbUserStateTimer;
import type.PotionType;
import util.MyUtil;

import java.util.Map;


/**
 * 使用药剂处理类
 *
 * @author 张丰博
 */
@Component
@Slf4j
public class UserPotionCmdHandler implements ICmdHandler<GameMsg.UserPotionCmd> {

    @Autowired
    private DbUserStateTimer userStateTimer;

    @Autowired
    private ImmediatelyResume immediatelyResume;

    @Autowired
    private SlowResume slowResume;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserPotionCmd userPotionCmd) {

        MyUtil.checkIsNull(ctx, userPotionCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        int location = userPotionCmd.getLocation();

        Map<Integer, Props> backpack = user.getBackpack();
        Props props = backpack.get(location);
        Potion potion = (Potion) props.getPropsProperty();

        if (potion == null || potion.getNumber() < 1) {
            //数量大于1
            throw new CustomizeException(CustomizeErrorCode.POTION_INSUFFICIENT);
        }
        if (potion.isCd()) {
            //cd时间中
            throw new CustomizeException(CustomizeErrorCode.POTION_CD_TIME);
        }

        UserPotionEntity userPotionEntity = new UserPotionEntity();
        userPotionEntity.setId(potion.getId());
        userPotionEntity.setNumber(potion.getNumber() - 1);
        userStateTimer.modifyUserPotion(userPotionEntity);

        GameMsg.UserPotionResult.Builder newBuilder = GameMsg.UserPotionResult.newBuilder();


        if (potion.getInfo().contains(PotionType.MP.getType())) {
            if (potion.getInfo().contains(PotionType.IMMEDIATELY.getType())) {
                immediatelyResume.immediatelyResumeMp(user, potion);
                newBuilder.setResumeMpEndTime(user.getUserResumeState().getEndTimeMp());
            } else if (potion.getInfo().contains(PotionType.SLOW.getType())) {
                slowResume.slowResumeMp(user, potion);
                newBuilder.setResumeMpEndTime(potion.getUsedEndTime())
                        .setResumeMpEndTimeAuto(user.getUserResumeState().getEndTimeMp());
            }
        } else if (potion.getInfo().contains(PotionType.HP.getType())) {
            if (potion.getInfo().contains(PotionType.IMMEDIATELY.getType())) {
                immediatelyResume.immediatelyResumeHp(user, potion);
                newBuilder.setResumeHpEndTime(user.getUserResumeState().getEndTimeHp());
            } else if (potion.getInfo().contains(PotionType.SLOW.getType())) {
                slowResume.slowResumeHp(potion);
                newBuilder.setResumeHpEndTime(potion.getUsedEndTime());
            }
        }

        newBuilder.setIsSuccess(true)
                .setLocation(location);
        // 设置使用药剂时间
        potion.setLastTimeSkillTime(System.currentTimeMillis());
        potion.setNumber(potion.getNumber() - 1);
        GameMsg.UserPotionResult potionResult = newBuilder.build();
        ctx.writeAndFlush(potionResult);

    }
}
