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
import server.cmdhandler.task.listener.TaskPublicMethod;
import server.model.PlayArena;
import server.model.User;
import server.model.UserManager;
import server.model.task.Task;
import util.MyUtil;

/**
 * @author 张丰博
 * 竞技场普通攻击
 */
@Component
@Slf4j
public class UserAttackCmdHandler implements ICmdHandler<GameMsg.UserAttackCmd> {

    @Autowired
    private TaskPublicMethod taskPublicMethod;

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
                    .setTargetUserId(targetUser.getUserId())
                    .build();
            ctx.writeAndFlush(userDieResult);

            throw new CustomizeException(CustomizeErrorCode.TARGET_USER_QUIT);
        }

        int subHp = user.calTargetUserSubHp(targetUser.getBaseDefense());


//        subHp = 10000;
        synchronized (targetUser.getMpMonitor()) {
            // 目标用户减血
            targetUser.setCurrHp(targetUser.getCurrHp() - subHp);
        }
        log.info("用户:{}, 对用户:{} 的伤害 {}", user.getUserName(), targetUser.getUserName(), subHp);

        ArenaUtil.getArenaUtil().sendMsg(user, targetUser, subHp);

        taskPublicMethod.listener(user);

    }
}
