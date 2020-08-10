package client.cmd;

import client.model.Role;
import client.model.SceneData;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;


import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 */
public class BackpackResultClient implements ICmd<GameMsg.BackpackResult> {

    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.BackpackResult backpackResult) {

    }
}
