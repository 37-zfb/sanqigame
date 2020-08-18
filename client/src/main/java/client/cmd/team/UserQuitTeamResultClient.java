package client.cmd.team;

import client.cmd.ICmd;
import client.model.PlayUserClient;
import client.model.Role;
import client.model.SceneData;
import client.model.team.PlayTeamClient;
import client.thread.CmdThread;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
@Slf4j
public class UserQuitTeamResultClient implements ICmd<GameMsg.UserQuitTeamResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserQuitTeamResult userQuitTeamResult) {
        MyUtil.checkIsNull(ctx, userQuitTeamResult);
        Role role = Role.getInstance();
        GameMsg.UserInfo userInfo = userQuitTeamResult.getUserInfo();
        int userId = userInfo.getUserId();
        String userName = userInfo.getUserName();

        PlayTeamClient team_client = role.getTEAM_CLIENT();
        if (userId == role.getId()){
            team_client.setTeamLeaderId(null);
            team_client.setOriginateUserId(null);
            team_client.setTeamMember(new PlayUserClient[4]);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        }else {
            PlayUserClient[] teamMember = team_client.getTeamMember();
            for (int i = 0; i < teamMember.length; i++) {
                if (teamMember[i].getUserId() == userId){
                    teamMember[i] = null;
                    log.info("用户 {} 退出队伍;", userName);
                    break;
                }
            }
        }

    }
}
