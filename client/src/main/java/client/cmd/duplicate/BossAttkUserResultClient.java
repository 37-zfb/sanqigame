package client.cmd.duplicate;

import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import model.duplicate.BossMonster;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
@Slf4j
public class BossAttkUserResultClient implements ICmd<GameMsg.BossAttkUserResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.BossAttkUserResult bossAttkUserResult) {
        MyUtil.checkIsNull(ctx, bossAttkUserResult);

        int subUserHp = bossAttkUserResult.getSubUserHp();
        Role role = Role.getInstance();
        BossMonster currBossMonster = role.getCurrDuplicate().getCurrBossMonster();
        role.setCurrHp(role.getCurrHp()-subUserHp);
        System.out.println("受到 "+currBossMonster.getBossName()+" 攻击, 血量减少: "+subUserHp);

    }
}
