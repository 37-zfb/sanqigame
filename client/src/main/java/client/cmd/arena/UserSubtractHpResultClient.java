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
public class UserSubtractHpResultClient implements ICmd<GameMsg.UserSubtractHpResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserSubtractHpResult userSubtractHpResult) {

        MyUtil.checkIsNull(ctx, userSubtractHpResult);

        int targetUserId = userSubtractHpResult.getTargetUserId();
        int subtractHp = userSubtractHpResult.getSubtractHp();

        Role role = Role.getInstance();
        PlayArenaClient playArenaClient = role.getARENA_CLIENT();

        if (role.getId() == targetUserId){
            // 受伤害的用户
            role.setCurrHp(role.getCurrHp() - subtractHp);
            System.out.println("受到攻击,减血:"+subtractHp);
        }else {
            // 发动攻击的用户
            PlayUserClient challengeUser = playArenaClient.getChallengeUser();
            challengeUser.setCurrHp(challengeUser.getCurrHp()-subtractHp);
            System.out.println("攻击 "+challengeUser.getUserName()+" 伤害 "+subtractHp);
            ArenaThread.getInstance().process(ctx, role);
        }

    }
}
