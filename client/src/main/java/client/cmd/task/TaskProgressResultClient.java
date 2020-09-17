package client.cmd.task;

import client.cmd.ICmd;
import client.model.Role;
import client.model.task.PlayTaskClient;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class TaskProgressResultClient implements ICmd<GameMsg.TaskProgressResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.TaskProgressResult taskProgressResult) {

        MyUtil.checkIsNull(ctx, taskProgressResult);
        Role role = Role.getInstance();

        PlayTaskClient playTaskClient = role.getPlayTaskClient();
        playTaskClient.setNumber(taskProgressResult.getNumber());
    }
}
