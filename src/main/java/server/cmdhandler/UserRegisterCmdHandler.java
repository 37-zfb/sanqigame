package server.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import server.async.AsyncService;


/**
 * @author 张丰博
 */
@Slf4j
public class UserRegisterCmdHandler implements ICmdHandler<GameMsg.UserRegisterCmd> {

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserRegisterCmd userRegisterCmd) {
        if (userRegisterCmd == null || ctx == null){
            return;
        }
        String newUserName = userRegisterCmd.getNewUserName();
        String newPassword = userRegisterCmd.getNewPassword();

        log.info("创建新用户:{}",newUserName);

        log.info("当前线程:{}",Thread.currentThread().getName());


        AsyncService.getInstance().asyn(newUserName,newPassword,false,(userEntity) -> {
            if (userEntity == null){
                log.info("用户:{},注册失败!",newUserName);
                return null;
            }

            // 构建返回结果
            GameMsg.UserRegisterResult registerResult =
                    GameMsg.UserRegisterResult.newBuilder()
                            .setNewUserName(userEntity.getUserName())
                            .build();
            ctx.writeAndFlush(registerResult);
            return null;
        });

    }
}
