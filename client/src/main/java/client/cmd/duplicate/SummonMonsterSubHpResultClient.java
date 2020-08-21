package client.cmd.duplicate;

import client.cmd.ICmd;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class SummonMonsterSubHpResultClient implements ICmd<GameMsg.SummonMonsterSubHpResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.SummonMonsterSubHpResult summonMonsterSubHpResult) {

        MyUtil.checkIsNull(ctx, summonMonsterSubHpResult);
        if (summonMonsterSubHpResult.getIsDie()){
            System.out.println("召唤兽已死;");
        }else {
            int subHp = summonMonsterSubHpResult.getSubHp();
            System.out.println("召唤兽掉血: "+subHp);
        }

    }
}
