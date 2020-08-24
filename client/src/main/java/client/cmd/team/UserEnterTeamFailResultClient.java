package client.cmd.team;

import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import client.thread.CmdThread;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserEnterTeamFailResultClient implements ICmd<GameMsg.UserEnterTeamFailResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserEnterTeamFailResult userEnterTeamFailResult) {
        MyUtil.checkIsNull(ctx, userEnterTeamFailResult);

        Role role = Role.getInstance();
        System.out.println("加入失败;");
        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());

    }
}
