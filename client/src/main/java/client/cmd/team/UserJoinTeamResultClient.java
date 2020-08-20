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

import java.util.List;

/**
 * @author 张丰博
 */
@Slf4j
public class UserJoinTeamResultClient implements ICmd<GameMsg.UserJoinTeamResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserJoinTeamResult userJoinTeamResult) {
        MyUtil.checkIsNull(ctx, userJoinTeamResult);
        Role role = Role.getInstance();

        boolean isJoin = userJoinTeamResult.getIsJoin();
        if (isJoin) {
            PlayTeamClient team_client = role.getTEAM_CLIENT();
            // 队长id
            int teamLeaderId = userJoinTeamResult.getTeamLeaderId();
            // 加入队伍
            // 双方都没有队伍
            if (role.getId() == teamLeaderId && team_client.getTeamLeaderId() == null) {
                // 此用户是队长，且还没有队伍
                team_client.setTeamLeaderId(role.getId());

                PlayUserClient[] teamMember = team_client.getTeamMember();
                teamMember[0] = new PlayUserClient(role.getId(), role.getUserName());

                List<GameMsg.UserInfo> userInfoList = userJoinTeamResult.getUserInfoList();
                for (GameMsg.UserInfo userInfo : userInfoList) {
                    teamMember[1] = new PlayUserClient(userInfo.getUserId(), userInfo.getUserName());
                }
                role.setTeam(false);
                role.setAnswer(false);
                log.info("和 {} 组队;",userInfoList.get(0).getUserName());
            }else if (role.getTEAM_CLIENT().getTeamLeaderId() != null){
                // 此用户有队伍, 队伍中增加队员
                PlayUserClient[] teamMember = team_client.getTeamMember();
                List<GameMsg.UserInfo> userInfoList = userJoinTeamResult.getUserInfoList();
                for (int i = 0; i < teamMember.length; i++) {
                    if (teamMember[i] == null){
                        teamMember[i] = new PlayUserClient(userInfoList.get(0).getUserId(),userInfoList.get(0).getUserName());
                        break;
                    }
                }
                role.setTeam(false);
                if (role.isAnswer()){
                    CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
                    role.setAnswer(false);
                }
                log.info("{} 加入队伍;",userInfoList.get(0).getUserName());
            }else if (role.getId() != teamLeaderId && team_client.getTeamLeaderId() == null){
                // 此用户没有队伍,刚加入队伍
                team_client.setTeamLeaderId(teamLeaderId);
                PlayUserClient[] teamMember = team_client.getTeamMember();
                List<GameMsg.UserInfo> userInfoList = userJoinTeamResult.getUserInfoList();
                teamMember[0] = new PlayUserClient(role.getId(),role.getUserName());
                for (int i = 0; i < userInfoList.size(); i++) {
                    teamMember[i+1] = new PlayUserClient(userInfoList.get(i).getUserId(),userInfoList.get(i).getUserName());
                }
                role.setTeam(false);
                role.setAnswer(false);
                CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
            }else {
                role.setTeam(false);
                CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
            }
            // 本用户有队伍,对方没有

            // 对方没有队伍,

            /*PlayTeamClient team_client = role.getTEAM_CLIENT();
            // 队长id
            int teamLeaderId = userJoinTeamResult.getTeamLeaderId();
            if (role.getId() == teamLeaderId && role.getTEAM_CLIENT().getTeamLeaderId() == null) {
                // 此用户是队长，且还没有队伍

                team_client.setTeamLeaderId(role.getId());

                PlayUserClient[] teamMember = team_client.getTeamMember();
                teamMember[0] = new PlayUserClient(role.getId(), role.getUserName());

                List<GameMsg.UserInfo> userInfoList = userJoinTeamResult.getUserInfoList();
                for (GameMsg.UserInfo userInfo : userInfoList) {
                    teamMember[1] = new PlayUserClient(userInfo.getUserId(), userInfo.getUserName());
                }
                System.out.println("队伍中增加: "+userInfoList.get(0).getUserName());
            } else if (role.getId() == teamLeaderId) {
                List<GameMsg.UserInfo> userInfoList = userJoinTeamResult.getUserInfoList();
                for (GameMsg.UserInfo userInfo : userInfoList) {
                    for (int i = 0; i < team_client.getTeamMember().length; i++) {
                        if (team_client.getTeamMember()[i] == null) {
                            team_client.getTeamMember()[i] = new PlayUserClient(userInfo.getUserId(), userInfo.getUserName());
                            break;
                        }
                    }
                }
                CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
            } else if (role.getId() != teamLeaderId && role.getTEAM_CLIENT().getTeamLeaderId() == null) {
                // 此用户不是队长，且还没有队伍； 此时刚组建队伍或新加入队伍的成员
                team_client.setTeamLeaderId(teamLeaderId);
                PlayUserClient[] teamMember = team_client.getTeamMember();
                List<GameMsg.UserInfo> userInfoList = userJoinTeamResult.getUserInfoList();

                teamMember[0] = new PlayUserClient(role.getId(), role.getUserName());
                for (GameMsg.UserInfo userInfo : userInfoList) {
                    for (int i = 1; i < teamMember.length; i++) {
                        if (teamMember[i] == null) {
                            teamMember[i] = new PlayUserClient(userInfo.getUserId(), userInfo.getUserName());
                            break;
                        }
                    }
                }

                CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
            } else if (role.getId() != teamLeaderId) {
                // 此用户不是队长，且已有队伍; 此时是队伍里增加人数
                PlayUserClient[] teamMember = team_client.getTeamMember();
                List<GameMsg.UserInfo> userInfoList = userJoinTeamResult.getUserInfoList();

                for (int i = 0; i < teamMember.length; i++) {
                    if (teamMember[i] == null) {
                        teamMember[i] = new PlayUserClient(userInfoList.get(0).getUserId(), userInfoList.get(0).getUserName());
                        break;
                    }
                }

                System.out.println("队伍增加: "+userInfoList.get(0).getUserName());
            }*/
        } else {
            System.out.println(role.getUserName() + " 拒绝组队");
        }

    }
}
