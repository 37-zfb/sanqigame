package server.cmdhandler.team;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayTeam;
import server.model.User;
import server.model.UserManager;
import util.MyUtil;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public class UserQuitTeamCmdHandler implements ICmdHandler<GameMsg.UserQuitTeamCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserQuitTeamCmd userQuitTeamCmd) {
        MyUtil.checkIsNull(ctx, userQuitTeamCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayTeam playTeam = user.getPlayTeam();
        Integer[] team_member = playTeam.getTEAM_MEMBER();


        for (int i = 0; i < team_member.length; i++) {
            if (team_member[i] != null && team_member[i].equals(user.getUserId())) {
                team_member[i] = null;
                break;
            }
        }
        user.setPlayTeam(null);
        GameMsg.UserInfo.Builder userInfo = GameMsg.UserInfo.newBuilder()
                .setUserId(user.getUserId())
                .setUserName(user.getUserName());
        GameMsg.UserQuitTeamResult userQuitTeamResult = GameMsg.UserQuitTeamResult.newBuilder()
                .setUserInfo(userInfo)
                .build();
        ctx.writeAndFlush(userQuitTeamResult);
        for (Integer id : team_member) {
            if (id != null){
                User userById = UserManager.getUserById(id);
                userById.getCtx().writeAndFlush(userQuitTeamResult);
            }
        }
    }
}
