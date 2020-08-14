package server.cmdhandler.duplicate;

import constant.DuplicateConst;
import constant.ProfessionConst;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.UserManager;
import server.timer.BossAttackTimer;
import util.MyUtil;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public class UserQuitDuplicateCmdHandler implements ICmdHandler<GameMsg.UserQuitDuplicateCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserQuitDuplicateCmd userQuitDuplicateCmd) {

        MyUtil.checkIsNull(ctx, userQuitDuplicateCmd);

        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        User user = UserManager.getUserById(userId);

        // 取消定时器
        BossAttackTimer.getInstance().cancelTask(user.getCurrDuplicate().getCurrBossMonster().getScheduledFuture());
        user.setCurrDuplicate(null);

        GameMsg.UserQuitDuplicateResult.Builder newBuilder = GameMsg.UserQuitDuplicateResult.newBuilder();
        if (user.getCurrHp() <= 0){
            newBuilder.setQuitDuplicateType(DuplicateConst.USER_KILLED);
        }else {
            newBuilder.setQuitDuplicateType(DuplicateConst.USER_NORMAL_QUIT_DUPLICATE);
        }
        user.setCurrHp(ProfessionConst.HP);
        user.setCurrMp(ProfessionConst.MP);
        // 用户退出
        GameMsg.UserQuitDuplicateResult userQuitDuplicateResult = newBuilder.build();

        ctx.writeAndFlush(userQuitDuplicateResult);

    }
}
