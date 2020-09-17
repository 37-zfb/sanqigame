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
 * 解散公会
 */
public class UserDissolveGuildResultClient implements ICmd<GameMsg.UserDissolveGuildResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserDissolveGuildResult userDissolveGuildResult) {

        MyUtil.checkIsNull(ctx, userDissolveGuildResult);
        Role role = Role.getInstance();
        System.out.println("公会已解散;");
        role.setPlayGuildClient(null);
        if (role.getId() == userDissolveGuildResult.getUserId()){
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        }

    }
}
