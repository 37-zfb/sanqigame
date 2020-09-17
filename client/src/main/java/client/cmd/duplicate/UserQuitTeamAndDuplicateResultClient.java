package client.cmd.duplicate;

import client.cmd.ICmd;
import client.model.PlayUserClient;
import client.model.Role;
import client.model.SceneData;
import client.model.team.PlayTeamClient;
import client.CmdThread;
import constant.ProfessionConst;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserQuitTeamAndDuplicateResultClient implements ICmd<GameMsg.UserQuitTeamAndDuplicateResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserQuitTeamAndDuplicateResult userQuitTeamAndDuplicateResult) {
        MyUtil.checkIsNull(ctx, userQuitTeamAndDuplicateResult);
        Role role = Role.getInstance();
        role.setCurrHp(ProfessionConst.HP);
        role.setCurrMp(ProfessionConst.MP);

        PlayTeamClient team_client = role.getTEAM_CLIENT();
        team_client.setTeamLeaderId(null);
        team_client.getOriginateIdSet().clear();
        team_client.setTeamMember(new PlayUserClient[4]);

        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
    }
}
