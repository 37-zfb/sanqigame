package client.cmd;

import client.model.Role;
import client.model.SceneData;
import client.thread.CmdThread;
import exception.CustomizeErrorCode;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class ErrorResultClient implements ICmd<GameMsg.ErrorResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.ErrorResult dealErrorResult) {
        MyUtil.checkIsNull(ctx, dealErrorResult);
        Role role = Role.getInstance();

        int code = dealErrorResult.getCode();
        if (code == CustomizeErrorCode.DEAL_REQUEST_ERROR.getCode()) {
            String msg = dealErrorResult.getMsg();
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
        } else if (code == CustomizeErrorCode.USER_NOT_DEAL_STATUS.getCode()) {
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        }


    }
}
