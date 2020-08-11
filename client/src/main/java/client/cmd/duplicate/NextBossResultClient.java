package client.cmd.duplicate;

import client.BossThread;
import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import model.duplicate.Duplicate;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class NextBossResultClient implements ICmd<GameMsg.NextBossResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.NextBossResult nextBossResult) {
        MyUtil.checkIsNull(ctx, nextBossResult);

        Role role = Role.getInstance();
        Duplicate currDuplicate = role.getCurrDuplicate();
        currDuplicate.setStartTime(nextBossResult.getStartTime());
        role.setEnter(true);
        currDuplicate.setMinBoss();

        BossThread.getInstance().process(ctx, role);

    }
}
