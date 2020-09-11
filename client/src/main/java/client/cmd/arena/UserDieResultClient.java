package client.cmd.arena;

import client.cmd.ICmd;
import client.model.Role;
import client.model.arena.PlayArenaClient;
import client.thread.ArenaThread;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserDieResultClient implements ICmd<GameMsg.UserDieResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserDieResult userDieResult) {

        MyUtil.checkIsNull(ctx, userDieResult);

        int targetUserId = userDieResult.getTargetUserId();

        Role role = Role.getInstance();
        PlayArenaClient playArenaClient = role.getARENA_CLIENT();

        if (role.getId()==targetUserId){
            // 死亡, 把挑战者用户设置为 null
            playArenaClient.setChallengeUser(null);
            System.out.println("被击杀;");
        }else {
            // 攻击者，
            playArenaClient.setChallengeUser(null);
            System.out.println("已击杀;");
//            ArenaThread.getInstance().process(ctx, role);
        }

        GameMsg.SortOutArenaCmd sortOutArenaCmd = GameMsg.SortOutArenaCmd.newBuilder().build();
        ctx.writeAndFlush(sortOutArenaCmd);
    }
}
