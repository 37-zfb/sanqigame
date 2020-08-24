package client.cmd.duplicate;

import client.model.server.duplicate.BossMonster;
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
public class AttkBossResultClient implements ICmd<GameMsg.AttkBossResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.AttkBossResult attkBossResult) {

        MyUtil.checkIsNull(ctx, attkBossResult);

        int subHp = attkBossResult.getSubHp();
        Role role = Role.getInstance();
        Duplicate currDuplicate = role.getCurrDuplicate();
        BossMonster currBossMonster = currDuplicate.getCurrBossMonster();
        currBossMonster.setHp(currBossMonster.getHp() - subHp);
        role.decreaseDurability();

        if (attkBossResult.getType().equals("召唤兽")) {
            System.out.println("召唤兽攻击 " + currBossMonster.getBossName() + " 减血 " + subHp);
        }

        if (role.getId() == attkBossResult.getUserId()) {
            BossThread.getInstance().process(ctx, role);
        } else {
            System.out.println(currBossMonster.getBossName() + " 减血 " + subHp);
        }
    }
}
