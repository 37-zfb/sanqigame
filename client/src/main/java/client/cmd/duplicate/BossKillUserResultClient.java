package client.cmd.duplicate;

import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;

/**
 * @author 张丰博
 */
public class BossKillUserResultClient implements ICmd<GameMsg.BossKillUserResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.BossKillUserResult bossKillUserResult) {

        Role role = Role.getInstance();
        synchronized (role.getHpMonitor()){
            // 被击杀
            role.setCurrHp(0);
        }
        role.setShieldValue(0);
        System.out.println("您已阵亡,副本: "+role.getCurrDuplicate().getName()+" ,Boss: "+role.getCurrDuplicate().getCurrBossMonster().getBossName());
        System.out.println("请推出副本;");
//        BossThread.getInstance().process(ctx, role);

    }
}
