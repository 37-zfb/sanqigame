package server.cmdhandler.team;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import util.MyUtil;

/**
 * @author 张丰博
 * 退出队伍
 */
@Component
@Slf4j
public class UserQuitTeamCmdHandler implements ICmdHandler<GameMsg.UserQuitTeamCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserQuitTeamCmd userQuitTeamCmd) {
        MyUtil.checkIsNull(ctx, userQuitTeamCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        TeamUtil.getTeamUtil().quitTeam(user);

    }
}
