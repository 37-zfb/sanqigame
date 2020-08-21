package server.cmdhandler.potionhandler;

import constant.PotionConst;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import model.props.Props;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import entity.db.UserPotionEntity;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.UserManager;
import model.props.Potion;
import server.service.UserService;
import type.PotionType;

import java.util.Map;


/**
 * 使用药剂
 *
 * @author 张丰博
 */
@Component
@Slf4j
public class UserPotionCmdHandler implements ICmdHandler<GameMsg.UserPotionCmd> {

    @Autowired
    private UserService userService;

    @Autowired
    private ImmediatelyResume immediatelyResume;

    @Autowired
    private SlowResume slowResume;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserPotionCmd userPotionCmd) {

        if (ctx == null || userPotionCmd == null) {
            return;
        }

        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        User user = UserManager.getUserById(userId);

        Map<Integer, Props> backpack = user.getBackpack();
        int location = userPotionCmd.getLocation();
        Props props = backpack.get(location);
        Potion potion = (Potion) props.getPropsProperty();
        boolean isSuccess;
        if (!potion.isCd()) {
            // 不在cd时间
            UserPotionEntity userPotionEntity = new UserPotionEntity(userId, props.getId(), potion.getNumber() - 1);
            isSuccess = userService.usePotion(userPotionEntity);
        } else {
            //在cd范围
            isSuccess = false;
        }

        GameMsg.UserPotionResult.Builder newBuilder = GameMsg.UserPotionResult.newBuilder();
        // 是否减库存成功
        if (isSuccess) {
            // 减数量
            potion.setNumber(potion.getNumber() - 1);
            // 立即恢复
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
                } else if (potion.getInfo().contains(PotionType.SLOW.getType())) {
                    slowResume.slowResumeHp(potion);
                    newBuilder.setResumeHpEndTime(potion.getUsedEndTime());
                }
            }
        }
        newBuilder.setIsSuccess(isSuccess)
                .setLocation(location);
        // 设置使用药剂时间
        potion.setLastTimeSkillTime(System.currentTimeMillis());
        potion.setNumber(potion.getNumber() - 1);
        GameMsg.UserPotionResult potionResult = newBuilder.build();
        ctx.writeAndFlush(potionResult);

    }
}
