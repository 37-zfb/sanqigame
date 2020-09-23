package server.cmdhandler;

import entity.db.UserEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.async.RegisterService;
/**
 * 用户注册
 *
 * @author 张丰博
 */
@Slf4j
@Component
public class UserRegisterCmdHandler implements ICmdHandler<GameMsg.UserRegisterCmd> {

    @Autowired
    private RegisterService registerService;

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


        registerService.asyn(userEntity, ctx, ()->{
            log.info("创建新用户:{}", userEntity.getUserName());

            GameMsg.UserRegisterResult userRegisterResult = GameMsg.UserRegisterResult
                    .newBuilder()
                    .setIsSucceed(true)
                    .build();
            ctx.writeAndFlush(userRegisterResult);
            return null;
        });



    }
}
