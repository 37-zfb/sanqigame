package server.cmdhandler.chat;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.model.User;
import server.model.UserManager;
import server.service.SensitiveFilterService;
import util.MyUtil;

/**
 * @author 张丰博
 * 私聊 处理类
 */
@Component
@Slf4j
public class PrivateChat extends Chat {



    @Override
    public void chat(ChannelHandlerContext ctx, GameMsg.UserChatInfoCmd userChatInfoCmd) {

        MyUtil.checkIsNull(ctx, userChatInfoCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        // 目标用户id
        int targetUserId = userChatInfoCmd.getTargetUserId();
        // 聊天内容
        String info = sensitiveWord(userChatInfoCmd);
        //聊天类型
        String chatType = userChatInfoCmd.getType();

        GameMsg.UserChatInfoResult chatInfoResult = GameMsg.UserChatInfoResult.newBuilder()
                .setUserName(user.getUserName())
                .setInfo(info)
                .setType(chatType)
                .build();

        // 私聊 ， 敏感词汇
        ChannelHandlerContext targetUserCtx = UserManager.getUserById(targetUserId).getCtx();

        targetUserCtx.writeAndFlush(chatInfoResult);
    }
}
