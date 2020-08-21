package client.cmd.duplicate;

import client.cmd.ICmd;
import client.model.Role;
import constant.ProfessionConst;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class PastorSkillResultClient implements ICmd<GameMsg.PastorSkillResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.PastorSkillResult pastorSkillResult) {

        MyUtil.checkIsNull(ctx, pastorSkillResult);

        Role role = Role.getInstance();

        synchronized (role.getHpMonitor()){
            if ((role.getCurrHp() + pastorSkillResult.getHp()) >= ProfessionConst.HP) {
                role.setCurrHp(ProfessionConst.HP);
            }else {
                role.setCurrHp(role.getCurrHp()+pastorSkillResult.getHp());
            }
            System.out.println("牧师加血,当前血量:"+role.getCurrHp());
        }


        synchronized (role.getMpMonitor()){
            if ((role.getCurrMp() + pastorSkillResult.getMp()) >= ProfessionConst.MP) {
                role.setCurrMp(ProfessionConst.MP);
            }else {
                role.setCurrMp(role.getCurrMp()+pastorSkillResult.getMp());
            }
            System.out.println("牧师加蓝,当前蓝量:"+role.getCurrMp());
        }




    }
}
