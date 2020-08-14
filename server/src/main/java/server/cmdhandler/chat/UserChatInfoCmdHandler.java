package server.cmdhandler.chat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.Broadcast;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.UserManager;
import type.ChatType;
import util.MyUtil;

/**
 * @author 张丰博
 *
 * 聊天处理类
 *  需要加密吗？？？
 *
 */
@Component
@Slf4j
public class UserChatInfoCmdHandler implements ICmdHandler<GameMsg.UserChatInfoCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserChatInfoCmd userChatInfoCmd) {
        MyUtil.checkIsNull(ctx, userChatInfoCmd);

        User user = PublicMethod.getInstance().getUser(ctx);

        // 目标用户id
        int targetUserId = userChatInfoCmd.getTargetUserId();
        // 聊天内容
        String info = userChatInfoCmd.getInfo();
        // 聊天类型
        String chatType = userChatInfoCmd.getType();


        GameMsg.UserChatInfoResult chatInfoResult = GameMsg.UserChatInfoResult.newBuilder()
                .setUserName(user.getUserName())
                .setInfo(info)
                .setType(chatType)
                .build();

        if (ChatType.PRIVATE_CHAT.getChatType().equals(chatType)){
            // 私聊
            ChannelHandlerContext targetUserCtx = UserManager.getUserById(targetUserId).getCtx();

            targetUserCtx.writeAndFlush(chatInfoResult);

        }else if (ChatType.PUBLIC_CHAT.getChatType().equals(chatType)){
            // 群聊

            Broadcast.allBroadcast(chatInfoResult);
        }




    }
}
