package server.cmdhandler;

import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import server.async.AsyncService;
import server.entity.RoleManager;

/**
 * @author 张丰博
 */
@Slf4j
public class UserLoginCmdHandler implements ICmdHandler<GameMsg.UserLoginCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserLoginCmd userLoginCmd) {

        if (ctx == null || userLoginCmd == null) {
            return;
        }

        String userName = userLoginCmd.getUserName();
        String password = userLoginCmd.getPassword();

        log.info("用户登陆:{}", userName);
        log.info("当前线程:{}", Thread.currentThread().getName());

        AsyncService.getInstance().asyn(userName, password, true, userEntity -> {

            log.info("登陆成功:userId={},userName={}", userEntity.getId(), userEntity.getUserName());
            log.info("当前线程:{}", Thread.currentThread().getName());

            // 添加到管理器中
            Role role = new Role();
            role.setId(userEntity.getId());
            role.setAddressName("启始之地");
            role.setBlood(100);
            role.setUserName(userEntity.getUserName());
            role.setX(0);
            role.setY(0);
            RoleManager.addRole(role);

            // 将用户id附着到channel中
//            ctx.channel().attr(AttributeKey.newInstance("userId")).set(userEntity.getId());

            // 登录结果构建
            GameMsg.UserLoginResult loginResult =
                    GameMsg.UserLoginResult.newBuilder()
                            .setUserId(role.getId())
                            .setUserName(role.getUserName())
                            .setHp(role.getBlood())
                            .setAddressName(role.getAddressName())
                            .setX(role.getX())
                            .setY(role.getY())
                            .build();

            ctx.writeAndFlush(loginResult);
            System.out.println("server=> UserLoginCmdHandler");
            return null;
        });

    }
}
