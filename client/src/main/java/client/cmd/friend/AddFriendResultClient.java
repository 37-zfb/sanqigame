package client.cmd.friend;

import client.cmd.ICmd;
import client.model.PlayFriendClient;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class AddFriendResultClient implements ICmd<GameMsg.AddFriendResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.AddFriendResult addFriendResult) {

        MyUtil.checkIsNull(ctx, addFriendResult);
        Role role = Role.getInstance();

        PlayFriendClient playFriendClient = role.getPlayFriendClient();
        playFriendClient.getFriendMap().put(addFriendResult.getUserId(), addFriendResult.getUserName());
        System.out.println("添加好友: "+addFriendResult.getUserName());
    }
}
