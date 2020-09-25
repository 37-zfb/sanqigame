package server.cmdhandler.arena;

import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.cmdhandler.task.listener.TaskUtil;
import server.model.PlayArena;
import server.model.User;
import server.UserManager;
import type.TaskType;
import util.MyUtil;

/**
 * @author 张丰博
 * 竞技场普通攻击
 */
@Component
@Slf4j
public class UserAttackCmdHandler implements ICmdHandler<GameMsg.UserAttackCmd> {

    @Autowired
    private TaskUtil taskPublicMethod;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserAttackCmd userAttackCmd) {
        MyUtil.checkIsNull(ctx, userAttackCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayArena playArena = user.getPlayArena();
        if (playArena == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_ARENA);
        }
        // 目标用户应减少的血量
        User targetUser = UserManager.getUserById(playArena.getTargetUserId());
        if (targetUser == null) {
            user.getPlayArena().setTargetUserId(null);
            GameMsg.UserDieResult userDieResult = GameMsg.UserDieResult.newBuilder()
                    .setTargetUserId(playArena.getTargetUserId())
                    .build();
            ctx.writeAndFlush(userDieResult);

            return;
        }

        int subHp = user.calTargetUserSubHp(targetUser.getBaseDefense());
//        subHp = 10000;
        synchronized (targetUser.getMpMonitor()) {
            // 目标用户减血
            synchronized (targetUser.getSHIELD_MONITOR()){
                if (targetUser.getShieldValue() > subHp) {
                    targetUser.setShieldValue(targetUser.getShieldValue() - subHp);

                    PublicMethod.getInstance().sendShieldMsg(subHp, targetUser);
                } else if (targetUser.getShieldValue() > 0 && targetUser.getShieldValue() < subHp) {
                    subHp -= targetUser.getShieldValue();
                    targetUser.setShieldValue(0);
                    PublicMethod.getInstance().sendShieldMsg(targetUser.getShieldValue(), targetUser);

                    targetUser.setCurrHp(targetUser.getCurrHp() - subHp);
                } else {
                    targetUser.setCurrHp(targetUser.getCurrHp() - subHp);
                }
            }
            ArenaUtil.getArenaUtil().sendMsg(user, targetUser, subHp);
        }
        log.info("用户:{}, 对用户:{} 的伤害 {}", user.getUserName(), targetUser.getUserName(), subHp);

        taskPublicMethod.listener(user, TaskType.PKWin.getTaskCode());
    }
}
