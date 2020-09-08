package client.cmd;

import client.GameClient;
import client.model.User;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import type.ProfessionType;

import java.util.Scanner;

/**
 * @author 张丰博
 */
@Slf4j
public class UserRegisterCmdClient implements ICmd<GameMsg.UserRegisterResult> {

    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserRegisterResult userRegisterResult) {
        if (ctx == null || userRegisterResult == null) {
            return;
        }

        UserLoginCmdClient loginCmdClient = new UserLoginCmdClient();
        User user = loginCmdClient.login();
        GameMsg.UserLoginCmd userLoginCmd = GameMsg.UserLoginCmd.newBuilder()
                .setUserName(user.getUserName())
                .setPassword(user.getPassword())
                .build();
        ctx.writeAndFlush(userLoginCmd);
    }

    /**
     * 注册
     */
    public User register() {
        Scanner scanner = new Scanner(System.in);
        String userName = null;
        String password = null;
        String confirmPassword = null;
        int professionId = 0;
        while (true) {
            System.out.print("============注册:请输入您的用户名: ");
            userName = scanner.nextLine();

            if ("".equals(userName)) {
                log.error("注册:用户名不能为空,请重新输入!");
                continue;
            }

            System.out.print("============注册:请输入您的密码: ");
            password = scanner.nextLine();

            if ("".equals(password)) {
                log.error("密码不能为空,请重新输入!");
                continue;
            }

            System.out.print("============注册:再次输入您的密码: ");
            confirmPassword = scanner.nextLine();

            if (!password.equals(confirmPassword)) {
                log.error("注册:两次密码输入不一致，请重新输入!");
            }else {
                System.out.println("============注册:请选择您从职业: ");
                for (ProfessionType professionType : ProfessionType.values()) {
                    System.out.println(professionType.getId() + "、" + professionType.getType());
                }
                professionId = scanner.nextInt();
            }

            break;
        }
        return new User(userName, password, professionId);

    }


}
