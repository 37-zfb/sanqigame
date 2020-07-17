package client.cmd;

import entity.User;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;

import java.util.Scanner;

/**
 * @author 张丰博
 */
@Slf4j
public class UserLoginCmdClient implements ICmd<GameMsg.UserLoginResult> {
     Scanner scanner = new Scanner(System.in);

    /**
     * 注册
     */
    public User register() {
        Scanner scanner = new Scanner(System.in);
        String userName = null;
        String password = null;
        String confirmPassword = null;
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
            }
            break;
        }
        return new User(userName, password);

    }

    /**
     * 登录
     */
    public User login() {
        Scanner scanner = new Scanner(System.in);
        String userName = null;
        String password = null;
        while (true) {
            System.out.print("============登录:请输入您的用户名: ");
            userName = scanner.nextLine();

            if ("".equals(userName)) {
                log.error("登录:用户名不能为空,请重新输入!");
                continue;
            }

            System.out.print("============登录:请输入您的密码: ");
            password = scanner.nextLine();

            if ("".equals(password)) {
                log.error("登录:密码不能为空,请重新输入!");
                continue;
            }
            break;
        }
        return new User(userName, password);


    }

    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserLoginResult userLoginResult) {

    }
}
