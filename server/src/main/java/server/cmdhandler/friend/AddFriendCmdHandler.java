package server.cmdhandler.friend;

import constant.FriendConst;
import entity.db.DbFriendEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.cmdhandler.task.listener.TaskPublicMethod;
import server.model.User;
import server.service.FriendService;
import server.util.IdWorker;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 * 添加好友
 */
@Component
@Slf4j
public class AddFriendCmdHandler implements ICmdHandler<GameMsg.AddFriendCmd> {
    @Autowired
    private FriendService friendService;

    @Autowired
    private TaskPublicMethod taskPublicMethod;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.AddFriendCmd addFriendCmd) {

        MyUtil.checkIsNull(ctx, addFriendCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        Map<Integer, String> friendMap = user.getPLAY_FRIEND().getFRIEND_MAP();
        if (friendMap.size() > FriendConst.FRIEND_MAX_NUMBER){
            //好友数量达到上限
            throw new CustomizeException(CustomizeErrorCode.FRIEND_REACH_LIMIT);
        }

        int targetUserId = addFriendCmd.getUserId();
        String targetUserName = addFriendCmd.getUserName();
        if (friendMap.values().contains(targetUserName)){
            throw new CustomizeException(CustomizeErrorCode.FRIEND_EXISTS);
        }


        friendMap.put(targetUserId, targetUserName);

        DbFriendEntity friendEntity = new DbFriendEntity();
        friendEntity.setId(IdWorker.generateId());
        friendEntity.setUserId(user.getUserId());
        friendEntity.setFriendId(targetUserId);
        friendEntity.setFriendName(targetUserName);
        friendService.addFriend(friendEntity);

        taskPublicMethod.listener(user);

        log.info("用户 {} 添加好友 {}", user.getUserName(),targetUserName);
        GameMsg.AddFriendResult addFriendResult = GameMsg.AddFriendResult
                .newBuilder()
                .setUserId(targetUserId)
                .setUserName(targetUserName)
                .build();
        ctx.writeAndFlush(addFriendResult);
    }
}
