package client.cmd;

import client.model.Role;
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
        System.out.println("您受到攻击: "+ attkCmd.getSubHp());
        Role role = Role.getInstance();
        role.setCurrHp(role.getCurrHp() - attkCmd.getSubHp());
//        CmdThread.getInstance().process(ctx, Role.getInstance(), SceneData.getInstance().getSceneMap().get(Role.getInstance().getCurrSceneId()).getNpcMap().values());

    }

}
