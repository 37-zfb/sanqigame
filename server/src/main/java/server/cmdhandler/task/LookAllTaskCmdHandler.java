package server.cmdhandler.task;

import entity.MailProps;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayTask;
import server.model.User;
import server.model.task.Task;
import server.scene.GameData;
import util.MyUtil;

import java.util.List;

/**
 * @author 张丰博
 * 查看任务
 */
@Component
@Slf4j
public class LookAllTaskCmdHandler implements ICmdHandler<GameMsg.LookAllTaskCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.LookAllTaskCmd lookAllTaskCmd) {

        MyUtil.checkIsNull(ctx, lookAllTaskCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayTask playTask = user.getPlayTask();
        Integer completedTaskId = playTask.getCompletedTaskId();

        if (!playTask.isHaveTask()){
            //此时没有任务了
            throw new CustomizeException(CustomizeErrorCode.TASK_NOT_FOUND);
        }

        Task task = GameData.getInstance().getTaskMap().get(completedTaskId + 1);

        log.info("用户 {} 查看任务;", user.getUserName());
        GameMsg.LookAllTaskResult lookAllTaskResult = GameMsg.LookAllTaskResult.newBuilder()
                .setTaskId(task.getId())
                .build();
        ctx.writeAndFlush(lookAllTaskResult);
    }
}
