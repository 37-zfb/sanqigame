package server.cmdhandler.duplicate;

import constant.DuplicateConst;
import constant.ProfessionConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import server.model.duplicate.Duplicate;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayTeam;
import server.model.User;
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

        User user = PublicMethod.getInstance().getUser(ctx);

        Duplicate currDuplicate;
        PlayTeam playTeam = user.getPlayTeam();
        if (playTeam == null ){
            currDuplicate = user.getCurrDuplicate();
            user.setCurrDuplicate(null);
        }else {
            currDuplicate = playTeam.getCurrDuplicate();
            playTeam.setCurrDuplicate(null);
        }
        if (currDuplicate != null){
            // 取消定时器
            BossAttackTimer.getInstance().cancelTask(currDuplicate.getCurrBossMonster().getScheduledFuture());
        }

        /**
         *  取消召唤师定时器
         */
        PublicMethod.getInstance().cancelSummonTimerOrPlayTeam(user);

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
