package server.cmdhandler.arena;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.UserManager;
import util.MyUtil;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public class UserAttackCmdHandler implements ICmdHandler<GameMsg.UserAttackCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserAttackCmd userAttackCmd) {
        MyUtil.checkIsNull(ctx, userAttackCmd);
        User user = PublicMethod.getInstance().getUser(ctx);
        int targetUserId = userAttackCmd.getTargetUserId();
        // 被攻击目标id
        User targetUser = UserManager.getUserById(targetUserId);

        if (user.getPlayArena().getTargetUserId() != targetUserId || targetUser.getPlayArena().getTargetUserId() != user.getUserId()) {

            return;
        }
        // 目标用户应减少的血量
        int subHp = user.calTargetUserSubHp(targetUser.getBaseDefense());
        subHp = 10000;
        // 在竞技场PK，只有玩家之间的伤害，并且都是在业务线程中
        synchronized (targetUser.getMpMonitor()) {
            // 目标用户减血
            targetUser.setCurrHp(targetUser.getCurrHp() - subHp);
        }
        log.info("用户:{}, 对用户:{} 的伤害 {}", user.getUserName(), targetUser.getUserName(), subHp);


//        GameMsg.UserAttackResult userAttackResult = GameMsg.UserAttackResult.newBuilder()
//                .setAttackUserId(user.getUserId())
//                .setTargetUserId(targetUserId)
//                .build();
//        ctx.writeAndFlush(userAttackResult);
//        targetUser.getCtx().writeAndFlush(userAttackResult);

        GameMsg.UserSubtractHpResult userSubtractHpResult = GameMsg.UserSubtractHpResult.newBuilder()
                .setTargetUserId(targetUserId)
                .setSubtractHp(subHp)
                .build();
        ctx.writeAndFlush(userSubtractHpResult);
        targetUser.getCtx().writeAndFlush(userSubtractHpResult);

        if (targetUser.getCurrHp() <= 0) {
            // 此时用户死了
            targetUser.getPlayArena().setTargetUserId(null);
            user.getPlayArena().setTargetUserId(null);
            GameMsg.UserDieResult userDieResult = GameMsg.UserDieResult.newBuilder()
                    .setTargetUserId(targetUserId)
                    .build();
            ctx.writeAndFlush(userDieResult);
            targetUser.getCtx().writeAndFlush(userDieResult);
        }
    }


}
