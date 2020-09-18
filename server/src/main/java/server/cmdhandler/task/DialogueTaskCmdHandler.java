package server.cmdhandler.task;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.cmdhandler.task.listener.TaskUtil;
import server.model.User;
import util.MyUtil;

/**
 * @author 张丰博
 * npc对话处理类
 */
@Component
@Slf4j
public class DialogueTaskCmdHandler implements ICmdHandler<GameMsg.DialogueTaskCmd> {

    @Autowired
    private TaskUtil taskPublicMethod;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.DialogueTaskCmd dialogueTaskCmd) {

        MyUtil.checkIsNull(ctx, dialogueTaskCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        taskPublicMethod.listener(user);
    }
}
