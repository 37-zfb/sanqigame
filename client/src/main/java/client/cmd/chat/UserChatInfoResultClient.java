package client.cmd.chat;

import client.thread.CmdThread;
import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import type.ChatType;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserChatInfoResultClient implements ICmd<GameMsg.UserChatInfoResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserChatInfoResult userChatInfoResult) {

        MyUtil.checkIsNull(ctx, userChatInfoResult);

        String userName = userChatInfoResult.getUserName();
        String info = userChatInfoResult.getInfo();
        String type = userChatInfoResult.getType();

        System.out.println("====>"+type+" 用户:"+userName+" "+info);
        Role role = Role.getInstance();
        if (ChatType.PUBLIC_CHAT.getChatType().equals(type) && role.isSelf()){
            role.setSelf(false);
            CmdThread.getInstance().process(ctx, role,SceneData.getInstance().getSceneMap().get(role.getId()).getNpcMap().values());
        }
    }
}
