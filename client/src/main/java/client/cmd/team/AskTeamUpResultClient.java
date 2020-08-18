package client.cmd.team;

import client.cmd.ICmd;
import client.model.Role;
import client.model.team.PlayTeamClient;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
@Slf4j
public class AskTeamUpResultClient implements ICmd<GameMsg.AskTeamUpResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.AskTeamUpResult askTeamUpResult) {

        MyUtil.checkIsNull(ctx, askTeamUpResult);
        Role role = Role.getInstance();

        PlayTeamClient teamClient = role.getTEAM_CLIENT();
        int originateUserId = askTeamUpResult.getOriginateUserId();
        teamClient.setOriginateUserId(originateUserId);
        String originateUserName = askTeamUpResult.getOriginateUserName();
        System.out.println(originateUserName+" 发起了组队请求;");
    }
}
