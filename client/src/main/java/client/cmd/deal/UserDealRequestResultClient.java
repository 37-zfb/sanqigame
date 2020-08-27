package client.cmd.deal;

import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import client.model.server.props.Props;
import client.thread.CmdThread;
import client.thread.DealThread;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import type.DealInfoType;
import type.PropsType;
import util.MyUtil;

import java.util.Map;
import java.util.Scanner;

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
                DealThread.getInstance().process(ctx, role);
            } else if (isSuccess && role.getId() != userDealRequestResult.getTargetUserId()) {
                // 成功， 发起者
                System.out.println("转换交易线程;");

            } else {
                System.out.println("交易失败;");
                if (userDealRequestResult.getTargetUserId() == role.getId()) {
                    CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
                }
            }

        }


    }


}
