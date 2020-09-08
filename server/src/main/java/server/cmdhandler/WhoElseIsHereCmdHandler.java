package server.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.model.User;
import server.model.UserManager;
import util.MyUtil;

import java.util.Collection;

/**
 * 查询当前场景实体
 * @author 张丰博
 */
@Slf4j
@Component
public class WhoElseIsHereCmdHandler implements ICmdHandler<GameMsg.WhoElseIsHereCmd> {

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.WhoElseIsHereCmd cmd) {

        MyUtil.checkIsNull(ctx, cmd);
        User curUser = PublicMethod.getInstance().getUser(ctx);

        Integer curSceneId = curUser.getCurSceneId();

        GameMsg.WhoElseIsHereResult.Builder resultBuilder = GameMsg.WhoElseIsHereResult.newBuilder();
        // 获得当前用户实体
        Collection<User> listUser = UserManager.listUser();
        for (User user : listUser) {
            if (user.getCurSceneId().equals(curSceneId)) {
                // 用户信息
                GameMsg.UserInfo userInfo =
                        GameMsg.UserInfo.newBuilder()
                                .setUserId(user.getUserId())
                                .setUserName(user.getUserName())
                                .build();
                resultBuilder.addUserInfo(userInfo);
            }
        }

        GameMsg.WhoElseIsHereResult whoElseIsHereResult = resultBuilder.build();
        ctx.channel().writeAndFlush(whoElseIsHereResult);
    }

}
