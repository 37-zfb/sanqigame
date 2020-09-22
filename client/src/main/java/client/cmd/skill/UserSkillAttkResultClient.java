package client.cmd.skill;

import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import client.model.server.duplicate.Duplicate;
import client.model.server.scene.Monster;
import client.model.server.scene.Scene;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserSkillAttkResultClient implements ICmd<GameMsg.UserSkillAttkResult> {

    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserSkillAttkResult userSkillAttkResult) {

        MyUtil.checkIsNull(ctx, userSkillAttkResult);

        Role role = Role.getInstance();
        Scene scene = SceneData.getInstance().getSceneMap().get(role.getCurrSceneId());

        // 重新设置恢复mp终止时间

        role.setCurrMp(role.getCurrMp()-userSkillAttkResult.getSubMp());
        role.getUserResumeState().setEndTimeMp(userSkillAttkResult.getResumeMpEndTime());
        role.startResumeMp();

        String noMaster = "no";

        if (noMaster.equals(userSkillAttkResult.getFalseReason())) {
            System.out.println(scene.getName() + " 的怪全部被击杀!");
        } else if (userSkillAttkResult.getIsSuccess()) {
            Duplicate currDuplicate = role.getCurrDuplicate();
            if (currDuplicate!=null){
                // 副本不为空
                return;
            }else {
                // 目标减血
                Monster monster = scene.getMonsterMap().get(userSkillAttkResult.getMonsterId());
                if (monster != null) {
                    monster.setHp(monster.getHp() - userSkillAttkResult.getSubtractHp());
                    System.out.println(monster.getName() + " hp: -" + userSkillAttkResult.getSubtractHp() + ", 剩余hp: " + monster.getHp());
                }
            }
        }

    }
}
