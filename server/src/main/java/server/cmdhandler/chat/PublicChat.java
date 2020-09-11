package server.cmdhandler.chat;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.Broadcast;
import server.PublicMethod;
import server.model.User;
import util.MyUtil;

/**
 * @author 张丰博
 * 公共聊天处理类
 */
@Component
@Slf4j
public class PublicChat extends Chat {

    @Override
    public void chat(ChannelHandlerContext ctx, GameMsg.UserChatInfoCmd userChatInfoCmd) {

        MyUtil.checkIsNull(ctx,userChatInfoCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        // 聊天内容
        String info = this.sensitiveWord(userChatInfoCmd);
        //聊天类型
        String chatType = userChatInfoCmd.getType();

        GameMsg.UserChatInfoResult chatInfoResult = GameMsg.UserChatInfoResult.newBuilder()
                .setUserName(user.getUserName())
                .setInfo(info)
                .setType(chatType)
                .build();
        // 群聊
        Broadcast.allBroadcast(chatInfoResult);
    }
}
