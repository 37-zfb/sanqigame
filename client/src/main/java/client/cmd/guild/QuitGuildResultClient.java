package client.cmd.guild;

import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import client.CmdThread;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class QuitGuildResultClient implements ICmd<GameMsg.QuitGuildResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.QuitGuildResult quitGuildResult) {

        MyUtil.checkIsNull(ctx, quitGuildResult);
        Role role = Role.getInstance();

        String userName = quitGuildResult.getUserName();
        if (role.getUserName().equals(userName)){
            role.setPlayGuildClient(null);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        }else {
            System.out.println(userName+" 退出公会;");
        }


    }
}
