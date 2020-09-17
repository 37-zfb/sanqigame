package server.cmdhandler.arena;

import constant.ProfessionConst;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.ArenaManager;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import util.MyUtil;

/**
 * @author 张丰博
 * 目标用户响应结果
 */
@Slf4j
@Component
public class TargetUserResponseCmdHandler implements ICmdHandler<GameMsg.TargetUserResponseCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.TargetUserResponseCmd targetUserResponseCmd) {
        MyUtil.checkIsNull(ctx, targetUserResponseCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        int originateUserId = targetUserResponseCmd.getOriginateUserId();
        boolean isAgree = targetUserResponseCmd.getIsAgree();

        user.getPlayArena().setTargetUserId(originateUserId);
        User originateUser = ArenaManager.getUserById(originateUserId);
        if (originateUser == null){
            throw new CustomizeException(CustomizeErrorCode.TARGET_NOT_EXIST);
        }

        GameMsg.UserChooseOpponentResult.Builder newBuilder = GameMsg.UserChooseOpponentResult.newBuilder();
        if (isAgree) {
            // 同意
            originateUser.getPlayArena().setTargetUserId(user.getUserId());
            user.getPlayArena().setTargetUserId(originateUserId);

            log.info("{} 和 {} 进行PK",user.getUserName(),originateUser.getUserName());
            newBuilder.setAcceptChallenge(true)
                    // 发起者id
            .setOriginateUserId(originateUserId)
                    // 被发起者id
            .setOriginatedUserId(user.getUserId())
            .setHp(ProfessionConst.HP)
            .setMp(ProfessionConst.MP);
        } else {
            //拒绝
            log.info("{} 拒绝了 {} PK请求;",user.getUserName(),originateUser.getUserName());
            newBuilder.setAcceptChallenge(false);
        }
        GameMsg.UserChooseOpponentResult userChooseOpponentResult = newBuilder
                .setOriginateUserName(originateUser.getUserName())
                .setOriginatedUserName(user.getUserName())
                .build();

        // 双方都通知结果
        ctx.writeAndFlush(userChooseOpponentResult);
        originateUser.getCtx().writeAndFlush(userChooseOpponentResult);
    }
}
