package client.cmd.deal;

import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserAddCompleteResultClient implements ICmd<GameMsg.UserAddCompleteResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserAddCompleteResult userAddCompleteResult) {

        MyUtil.checkIsNull(ctx, userAddCompleteResult);
        Role role = Role.getInstance();
        GameMsg.UserInfo userInfo = userAddCompleteResult.getUserInfo();
        String userName = userInfo.getUserName();
        System.out.println("用户: "+userName+" 添加完毕;");
    }
}
