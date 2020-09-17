package client.cmd.team;

import client.cmd.ICmd;
import client.model.PlayUserClient;
import client.model.Role;
import client.model.team.PlayTeamClient;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import util.MyUtil;

import java.util.List;

/**
 * @author 张丰博
 *
 */
@Slf4j
public class UserJoinTeamPerformResultClient implements ICmd<GameMsg.UserJoinTeamPerformResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserJoinTeamPerformResult userJoinTeamPerformResult) {
        MyUtil.checkIsNull(ctx, userJoinTeamPerformResult);
        Role role = Role.getInstance();

        boolean isJoin = userJoinTeamPerformResult.getIsJoin();


        if (isJoin) {
            PlayTeamClient team_client = role.getTEAM_CLIENT();
            // 队长id
            int teamLeaderId = userJoinTeamPerformResult.getTeamLeaderId();
            // 加入队伍
            // 双方都没有队伍
            if (role.getId() == teamLeaderId && team_client.getTeamLeaderId() == null) {
                // 此用户是队长，创建队伍
                team_client.setTeamLeaderId(role.getId());

                PlayUserClient[] teamMember = team_client.getTeamMember();
                PlayUserClient playUserClient = new PlayUserClient(role.getId(), role.getUserName());
                playUserClient.setCurrHp(role.getCurrHp());
                playUserClient.setCurrMp(role.getCurrMp());
                teamMember[0] = playUserClient;
                role.setTeam(false);
                role.setAnswer(false);
            }else if (role.getTEAM_CLIENT().getTeamLeaderId() != null){
                // 此用户有队伍, 队伍中增加队员
                PlayUserClient[] teamMember = team_client.getTeamMember();
                List<GameMsg.UserInfo> userInfoList = userJoinTeamPerformResult.getUserInfoList();
                for (int i = 0; i < teamMember.length; i++) {
                    if (teamMember[i] == null){
                        GameMsg.UserInfo userInfo = userInfoList.get(0);
                        PlayUserClient playUserClient = new PlayUserClient(userInfo.getUserId(), userInfo.getUserName());
                        playUserClient.setCurrHp(userInfo.getCurrHp());
                        playUserClient.setCurrMp(userInfo.getCurrMp());
                        teamMember[i] = playUserClient;
                        break;
                    }
                }
                role.setTeam(false);
                if (role.isAnswer()){
                    role.setAnswer(false);
                }
                log.info("{} 加入队伍;",userInfoList.get(0).getUserName());
            }else if (role.getId() != teamLeaderId && team_client.getTeamLeaderId() == null){
                // 此用户没有队伍,刚加入队伍
                team_client.setTeamLeaderId(teamLeaderId);
                PlayUserClient[] teamMember = team_client.getTeamMember();
                List<GameMsg.UserInfo> userInfoList = userJoinTeamPerformResult.getUserInfoList();
                teamMember[0] = new PlayUserClient(role.getId(),role.getUserName(),role.getCurrMp(),role.getCurrHp());

                for (int i = 0; i < userInfoList.size(); i++) {
                    GameMsg.UserInfo userInfo = userInfoList.get(i);
                    teamMember[i+1] = new PlayUserClient(userInfo.getUserId(),userInfo.getUserName(),userInfo.getCurrMp(),userInfo.getCurrHp());
                }
                role.setTeam(false);
                role.setAnswer(false);
            }else {
                role.setTeam(false);
            }

        } else {
            System.out.println("加入队伍失败;");
        }


    }
}
