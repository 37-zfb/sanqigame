package server.cmdhandler.friend;

import entity.db.DbFriendEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.service.FriendService;
import util.MyUtil;

/**
 * @author 张丰博
 * 删除好友
 */
@Component
@Slf4j
public class DeleteFriendCmdHandler implements ICmdHandler<GameMsg.DeleteFriendCmd> {

    @Autowired
    private FriendService friendService;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.DeleteFriendCmd deleteFriendCmd) {

        MyUtil.checkIsNull(ctx, deleteFriendCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        int targetUserId = deleteFriendCmd.getUserId();
        String removeName = user.getPLAY_FRIEND().getFRIEND_MAP().remove(targetUserId);

        DbFriendEntity friendEntity = new DbFriendEntity();
        friendEntity.setUserId(user.getUserId());
        friendEntity.setFriendId(targetUserId);

        friendService.deleteFriend(friendEntity);

        log.info("用户 {} 删除 好友 {}",user.getUserName(),removeName);
        GameMsg.DeleteFriendResult deleteFriendResult = GameMsg.DeleteFriendResult.newBuilder()
                .setUserId(targetUserId)
                .build();
        ctx.writeAndFlush(deleteFriendResult);
    }
}
