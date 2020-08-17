package client.cmd.arena;

import client.cmd.ICmd;
import client.model.Role;
import client.model.arena.ArenaUser;
import client.model.arena.PlayArenaClient;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class TargetUserChallengeResultClient implements ICmd<GameMsg.TargetUserChallengeResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.TargetUserChallengeResult targetUserChallengeResult) {
        MyUtil.checkIsNull(ctx, targetUserChallengeResult);
        Role role = Role.getInstance();

        int originateUserId = targetUserChallengeResult.getOriginateUserId();
        PlayArenaClient playArenaClient = role.getPlayArenaClient();
        // 设置挑战者id
        playArenaClient.setOriginateUserId(originateUserId);
        ArenaUser arenaUser = playArenaClient.getArenaUserMap().get(originateUserId);
        System.out.println("收到 "+arenaUser.getUserName() +" 的挑战;");


    }
}
