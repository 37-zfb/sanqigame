package server.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import org.example.msg.GameMsg;

/**
 * @author 张丰博
 */
public class UserLoginCmdHandler implements ICmdHandler<GameMsg.UserLoginCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserLoginCmd userLoginCmd) {

        if (ctx==null || userLoginCmd == null){
            return;
        }

        String userName = userLoginCmd.getUserName();
        String password = userLoginCmd.getPassword();


    }
}
