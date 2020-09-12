package client.cmd.duplicate;

import client.model.server.duplicate.Duplicate;
import client.thread.BossThread;
import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
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
        // 减耐久度
        role.decreaseDurability();

        Duplicate currDuplicate = role.getCurrDuplicate();
        currDuplicate.setStartTime(nextBossResult.getStartTime());
        currDuplicate.setMinBoss();
        if (role.getId() == nextBossResult.getUserId()){

//            BossThread.getInstance().process(ctx, role);
        }else {
            System.out.println("进入下一个BOSS "+currDuplicate.getCurrBossMonster().getBossName());
        }
    }
}
