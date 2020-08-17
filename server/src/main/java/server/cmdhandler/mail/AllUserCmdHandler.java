package server.cmdhandler.mail;

import entity.db.UserEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.service.UserService;
import util.MyUtil;

import java.util.List;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public class AllUserCmdHandler implements ICmdHandler<GameMsg.AllUserCmd> {
    @Autowired
    private UserService userService;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.AllUserCmd allUserCmd) {
        MyUtil.checkIsNull(ctx, allUserCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        List<UserEntity> listUser = userService.listUser();

        GameMsg.AllUserResult.Builder newBuilder = GameMsg.AllUserResult.newBuilder();
        for (UserEntity userEntity : listUser) {
            GameMsg.UserInfo.Builder builder = GameMsg.UserInfo.newBuilder()
                    .setUserId(userEntity.getId())
                    .setUserName(userEntity.getUserName());
            newBuilder.addUserInfo(builder);
        }

        GameMsg.AllUserResult allUserResult = newBuilder.build();
        ctx.writeAndFlush(allUserResult);

    }
}
