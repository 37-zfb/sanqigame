package client.cmd.arena;

import client.cmd.ICmd;
import client.model.Role;
import client.model.PlayUserClient;
import client.model.arena.PlayArenaClient;
import client.module.ArenaModule;
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

        int hp = userChooseOpponentResult.getHp();
        int mp = userChooseOpponentResult.getMp();
        String originatedUserName = userChooseOpponentResult.getOriginatedUserName();
        String originateUserName = userChooseOpponentResult.getOriginateUserName();

        if (acceptChallenge) {
            // 接受挑战
            PlayUserClient originateUser = playArenaClient.getArenaUserMap().get(originateUserId);
            PlayUserClient originatedUser = playArenaClient.getArenaUserMap().get(originatedUserId);
            if (role.getId() == originateUserId) {
                // 发起者id
                playArenaClient.setChallengeUser(
                        new PlayUserClient(originatedUser.getUserId(),
                                originatedUser.getUserName(),
                                mp,
                                hp));
                System.out.println(originateUserName + " 、 " + originatedUserName + " 开始PK;");
            } else if (role.getId() == originatedUserId) {
                // 被发起者
                playArenaClient.setChallengeUser(
                        new PlayUserClient(originateUser.getUserId(),
                                originateUser.getUserName(),
                                mp,
                                hp));
                System.out.println(originatedUserName + " 、 " + originateUserName + " 开始PK;");
            }
        } else {
            // 拒绝挑战
//            System.out.println("拒绝组队;");
            System.out.println(originatedUserName + " 拒绝了 " + originateUserName + " 的PK邀请;");
        }

//        ArenaModule.getInstance().process(ctx, role);

    }
}
