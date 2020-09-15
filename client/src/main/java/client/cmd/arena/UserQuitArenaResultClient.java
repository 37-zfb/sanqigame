package client.cmd.arena;

import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import client.model.arena.PlayArenaClient;
import client.thread.CmdThread;
import constant.ProfessionConst;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserQuitArenaResultClient implements ICmd<GameMsg.UserQuitArenaResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserQuitArenaResult userQuitArenaResult) {

        MyUtil.checkIsNull(ctx, userQuitArenaResult);
        Role role = Role.getInstance();

        GameMsg.UserInfo userInfo = userQuitArenaResult.getUserInfo();
        String userName = userInfo.getUserName();
        int userId = userInfo.getUserId();
        PlayArenaClient playArenaClient = role.getARENA_CLIENT();

        if (userId == role.getId()){
            // 标识未进竞技场; 本用户
            playArenaClient.setInArena(false);
            role.setCurrHp(ProfessionConst.HP);
            role.setCurrMp(ProfessionConst.MP);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        }else {
            // 其他用户，
            playArenaClient.getArenaUserMap().remove(userId);
        }

    }
}
