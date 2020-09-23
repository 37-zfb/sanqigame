package client.cmd.friend;

import client.CmdThread;
import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class DeleteFriendResultClient implements ICmd<GameMsg.DeleteFriendResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.DeleteFriendResult deleteFriendResult) {

        MyUtil.checkIsNull(ctx, deleteFriendResult);
        Role role = Role.getInstance();

        String removeName = role.getPlayFriendClient().getFriendMap().remove(deleteFriendResult.getUserId());
        System.out.println("删除好友: "+removeName);
        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
    }
}
