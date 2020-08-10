package server.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import entity.db.UserEntity;
import server.service.UserService;


/**
 *
 * 用户注册
 *
 * @author 张丰博
 */
@Slf4j
@Component
public class UserRegisterCmdHandler implements ICmdHandler<GameMsg.UserRegisterCmd> {

    @Autowired
    private UserService userService;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserRegisterCmd userRegisterCmd) {
        if (userRegisterCmd == null || ctx == null) {
            return;
        }

        //封装用户基本信息
        UserEntity userEntity = new UserEntity();
        userEntity.setUserName(userRegisterCmd.getNewUserName());
        userEntity.setPassword(userRegisterCmd.getNewPassword());
        userEntity.setProfessionId(userRegisterCmd.getProfessionId());


        GameMsg.UserRegisterResult.Builder registerResultBuilder = GameMsg.UserRegisterResult.newBuilder();
        GameMsg.UserRegisterResult registerResult = null;

        try {
            userService.addUser(userEntity);

            registerResultBuilder.setIsSucceed(true);
            log.info("创建新用户:{}", userEntity.getUserName());
            log.info("当前线程:{}", Thread.currentThread().getName());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            registerResultBuilder.setIsSucceed(false);

        } finally {
            // 构建返回结果
            registerResult = registerResultBuilder.build();
            ctx.channel().writeAndFlush(registerResult);
        }


    }
}
