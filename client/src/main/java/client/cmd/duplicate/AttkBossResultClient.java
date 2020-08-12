package client.cmd.duplicate;

import client.BossThread;
import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import model.duplicate.BossMonster;
import model.duplicate.Duplicate;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class AttkBossResultClient implements ICmd<GameMsg.AttkBossResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.AttkBossResult attkBossResult) {

        MyUtil.checkIsNull(ctx, attkBossResult);

        int subHp = attkBossResult.getSubHp();
        Role role = Role.getInstance();
        Duplicate currDuplicate = role.getCurrDuplicate();
        BossMonster currBossMonster = currDuplicate.getCurrBossMonster();
        currBossMonster.setHp(currBossMonster.getHp()-subHp);


        BossThread.getInstance().process(ctx, role);
    }
}
