package client.cmd;

import client.model.server.duplicate.Duplicate;
import client.model.server.scene.Monster;
import client.model.server.scene.Scene;
import client.thread.BossThread;
import client.thread.CmdThread;
import client.model.Role;
import client.model.SceneData;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;

/**
 * @author 张丰博
 */
public class UserSkillAttkResultClient implements ICmd<GameMsg.UserSkillAttkResult> {

    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserSkillAttkResult userSkillAttkResult) {

        if (ctx == null || userSkillAttkResult == null) {
            return;
        }

        Role role = Role.getInstance();
        Scene scene = SceneData.getInstance().getSceneMap().get(role.getCurrSceneId());

        // 重新设置恢复mp终止时间
        role.getUserResumeState().setEndTimeMp(userSkillAttkResult.getResumeMpEndTime());
        role.startResumeMp();

        String noMaster = "no";
        String mpInsufficient = "mp";

        if (noMaster.equals(userSkillAttkResult.getFalseReason())) {
            System.out.println(scene.getName() + " 的怪全部被击杀!");
        } else if (mpInsufficient.equals(userSkillAttkResult.getFalseReason())) {
            System.out.println("mp 不足!");
        } else if (userSkillAttkResult.getIsSuccess()) {
            Duplicate currDuplicate = role.getCurrDuplicate();
            if (currDuplicate!=null){
                // 副本不为空



                BossThread.getInstance().process(ctx, role);
                return;
            }else {
                // 目标减血
                Monster monster = scene.getMonsterMap().get(userSkillAttkResult.getMonsterId());
                if (monster != null) {
                    monster.setHp(monster.getHp() - userSkillAttkResult.getSubtractHp());
                    System.out.println(monster.getName() + " hp: -" + userSkillAttkResult.getSubtractHp() + ", 剩余hp: " + monster.getHp());
                }
            }
        } else {
            if (scene.getMonsterMap().size() == 0) {
                System.out.println("当前场景没有怪!");
            } else {
                System.out.println("技能冷却状态!");
            }
        }

        CmdThread.getInstance().process(ctx, role, scene.getNpcMap().values());
    }
}
