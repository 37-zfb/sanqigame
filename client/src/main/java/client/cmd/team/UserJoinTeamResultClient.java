package client.cmd.team;

import client.cmd.ICmd;
import client.model.PlayUserClient;
import client.model.Role;
import client.model.SceneData;
import client.model.team.PlayTeamClient;
import client.thread.CmdThread;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import util.MyUtil;

import java.util.List;

/**
 * @author 张丰博
 */
@Slf4j
public class UserJoinTeamResultClient implements ICmd<GameMsg.UserJoinTeamResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserJoinTeamResult userJoinTeamResult) {
        MyUtil.checkIsNull(ctx, userJoinTeamResult);
        Role role = Role.getInstance();

        boolean isJoin = userJoinTeamResult.getIsJoin();
        int targetId = userJoinTeamResult.getTargetId();
        GameMsg.UserJoinTeamPerformCmd userJoinTeamPerform = GameMsg.UserJoinTeamPerformCmd.newBuilder()
                .setTargetId(targetId)
                .setIsAgree(isJoin)
                .build();
        ctx.writeAndFlush(userJoinTeamPerform);

        if (!isJoin) {
            String targetName = userJoinTeamResult.getTargetName();
            System.out.println(targetName + " 拒绝加入队伍;");
        }

    }
}
