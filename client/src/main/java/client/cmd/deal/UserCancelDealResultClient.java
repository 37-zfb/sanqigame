package client.cmd.deal;

import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import client.CmdThread;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserCancelDealResultClient implements ICmd<GameMsg.UserCancelDealResult> {

    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserCancelDealResult userCancelDealResult) {
        MyUtil.checkIsNull(ctx, userCancelDealResult);
        Role role = Role.getInstance();
        role.getDEAL_CLIENT().setDealState(false);
        boolean isSuccess = userCancelDealResult.getIsSuccess();
        if (isSuccess) {
            int userId = userCancelDealResult.getUserId();
            if (userId != role.getId()) {
                System.out.println("对方取消交易;");
                GameMsg.UserCancelDealCmd userCancelDealCmd = GameMsg.UserCancelDealCmd
                        .newBuilder()
                        .setIsNeedNotice(false)
                        .build();
                ctx.writeAndFlush(userCancelDealCmd);
            }
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        }

    }
}
