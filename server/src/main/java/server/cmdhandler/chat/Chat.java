package server.cmdhandler.chat;

import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import server.service.SensitiveFilterService;

/**
 * @author 张丰博
 *  聊天接口
 */
public abstract class Chat {

    @Autowired
    private SensitiveFilterService sensitiveFilterService;

    /**
     *  聊天处理类
     * @param ctx
     * @param userChatInfoCmd
     */
    abstract void chat(ChannelHandlerContext ctx, GameMsg.UserChatInfoCmd userChatInfoCmd);

    /**
     * 过滤后的消息
     * @param userChatInfoCmd
     * @return
     */
    protected String sensitiveWord(GameMsg.UserChatInfoCmd userChatInfoCmd){
        return sensitiveFilterService.replaceSensitiveWord(userChatInfoCmd.getInfo(),1,"*");
    }

}
