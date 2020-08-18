package client.cmd.arena;

import client.cmd.ICmd;
import client.model.Role;
import client.model.PlayUserClient;
import client.model.arena.PlayArenaClient;
import client.thread.ArenaThread;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */

public class UserChooseOpponentResultClient implements ICmd<GameMsg.UserChooseOpponentResult> {

    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserChooseOpponentResult userChooseOpponentResult) {
        MyUtil.checkIsNull(ctx, userChooseOpponentResult);
        boolean acceptChallenge = userChooseOpponentResult.getAcceptChallenge();
        int originateUserId = userChooseOpponentResult.getOriginateUserId();
        int originatedUserId = userChooseOpponentResult.getOriginatedUserId();

        Role role = Role.getInstance();
        PlayArenaClient playArenaClient = role.getARENA_CLIENT();

        if (acceptChallenge){
            // 接受挑战
            PlayUserClient originateUser = playArenaClient.getArenaUserMap().get(originateUserId);
            PlayUserClient originatedUser = playArenaClient.getArenaUserMap().get(originatedUserId);
            if (role.getId() == originateUserId){
                // 发起者id
                playArenaClient.setChallengeUser(originatedUser);
            }else if (role.getId() == originatedUserId){
                // 被发起者
                playArenaClient.setChallengeUser(originateUser);
            }
        }else {
            // 拒绝挑战

        }

        ArenaThread.getInstance().process(ctx, role);

    }
}
