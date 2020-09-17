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
public class UserDealRequestResultClient implements ICmd<GameMsg.UserDealRequestResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserDealRequestResult userDealRequestResult) {

        MyUtil.checkIsNull(ctx, userDealRequestResult);
        Role role = Role.getInstance();

        boolean isAgree = userDealRequestResult.getIsAgree();

        if (!isAgree) {
            // 拒绝交易
            if (role.getId().equals(userDealRequestResult.getTargetUserId())) {
                CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
            } else {
                System.out.println("用户: " + userDealRequestResult.getTargetUserName() + " 拒绝交易;");
            }
        } else {
            // 同意交易
            boolean isSuccess = userDealRequestResult.getIsSuccess();
            if (isSuccess && role.getId() == userDealRequestResult.getTargetUserId()) {
                // 进入交易线程

                role.getDEAL_CLIENT().setDealState(true);
                CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());

//                DealThread.getInstance().process(ctx, role);
            } else if (isSuccess && role.getId() != userDealRequestResult.getTargetUserId()) {
                // 成功， 发起者
                GameMsg.UserModifyDealStateCmd userModifyDealStateCmd = GameMsg.UserModifyDealStateCmd.newBuilder()
                        .setTargetId(userDealRequestResult.getTargetUserId())
                        .build();
                ctx.writeAndFlush(userModifyDealStateCmd);
            }

        }


    }


}
