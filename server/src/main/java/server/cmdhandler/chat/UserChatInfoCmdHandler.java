package server.cmdhandler.chat;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.Broadcast;
import server.GameServer;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.UserManager;
import server.service.SensitiveFilterService;
import type.ChatType;
import util.MyUtil;

/**
 * @author 张丰博
 *
 * 聊天处理类
 *
 */
@Component
@Slf4j
public class UserChatInfoCmdHandler implements ICmdHandler<GameMsg.UserChatInfoCmd> {

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserChatInfoCmd userChatInfoCmd) {
        MyUtil.checkIsNull(ctx, userChatInfoCmd);

        // 聊天类型
        String chatType = userChatInfoCmd.getType();
        String info = userChatInfoCmd.getInfo();

        for (ChatType type : ChatType.values()) {
            if (type.getChatType().equals(chatType)){
                try {
                    Chat chat = (Chat) GameServer.APPLICATION_CONTEXT.getBean(Class.forName(type.getHandler()));                    chat.chat(ctx,userChatInfoCmd);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
