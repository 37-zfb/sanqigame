package server.cmdhandler.chat;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.GameServer;
import server.cmdhandler.ICmdHandler;
import type.ChatType;
import util.MyUtil;

/**
 * @author 张丰博
 * <p>
 * 聊天处理类
 */
@Component
@Slf4j
public class UserChatInfoCmdHandler implements ICmdHandler<GameMsg.UserChatInfoCmd> {

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserChatInfoCmd userChatInfoCmd) {
        MyUtil.checkIsNull(ctx, userChatInfoCmd);

        // 聊天类型
        String chatType = userChatInfoCmd.getType();
        if (chatType == null || "".equals(chatType)) {
            return;
        }

        for (ChatType type : ChatType.values()) {
            if (!type.getChatType().equals(chatType)) {
                continue;
            }

            try {
                Chat chat = (Chat) GameServer.APPLICATION_CONTEXT.getBean(Class.forName(type.getHandler()));
                chat.chat(ctx, userChatInfoCmd);
            } catch (ClassNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        }

    }
}
