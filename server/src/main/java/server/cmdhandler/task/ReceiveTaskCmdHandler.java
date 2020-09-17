package server.cmdhandler.task;

import io.netty.channel.ChannelHandlerContext;
import lombok.Setter;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.cmdhandler.ICmdHandler;
import server.cmdhandler.task.listener.TaskUtil;
import util.MyUtil;

/**
 * @author 张丰博
 * 领取任务
 */
@Component
@Setter
public class ReceiveTaskCmdHandler implements ICmdHandler<GameMsg.ReceiveTaskCmd> {

    @Autowired
    private TaskUtil receiveTask;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.ReceiveTaskCmd receiveTaskCmd) {

        MyUtil.checkIsNull(ctx, receiveTaskCmd);

        receiveTask.receiveTask(ctx, receiveTaskCmd.getTaskId());
    }
}
