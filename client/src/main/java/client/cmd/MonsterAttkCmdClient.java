package client.cmd;

import client.CmdThread;
import client.model.Role;
import client.model.SceneData;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;

/**
 * @author 张丰博
 */
public class MonsterAttkCmdClient implements ICmd<GameMsg.AttkCmd> {

    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.AttkCmd attkCmd) {
        if (ctx == null || attkCmd == null) {
            return;
        }
        System.out.println("您受到攻击,Hp-2");


        CmdThread.getInstance().process(ctx, Role.getInstance(), SceneData.getInstance().getSceneMap().get(Role.getInstance().getCurrSceneId()).getNpcMap().values());

    }

}
