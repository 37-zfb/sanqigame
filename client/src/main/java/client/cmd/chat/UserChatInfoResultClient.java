package client.cmd.chat;

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
public class UserChatInfoResultClient implements ICmd<GameMsg.UserChatInfoResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserChatInfoResult userChatInfoResult) {

        MyUtil.checkIsNull(ctx, userChatInfoResult);

        String userName = userChatInfoResult.getUserName();
        String info = userChatInfoResult.getInfo();
        String type = userChatInfoResult.getType();

        System.out.println("====>"+type+" 用户:"+userName+" "+info);

        CmdThread.getInstance().process(ctx, Role.getInstance(), SceneData.getInstance().getSceneMap().get(Role.getInstance().getCurrSceneId()).getNpcMap().values());
    }
}
