package client.cmd;

import client.model.Role;
import client.model.User;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;

import java.util.Scanner;

/**
 * @author 张丰博
 */
@Slf4j
public class UserLoginCmdClient implements ICmd<GameMsg.UserLoginResult> {

    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserLoginResult userLoginResult) {

        if (ctx== null || userLoginResult == null){
            return;
        }

//        System.out.println("============欢迎勇士来到冒险大陆==============");

        Role role = new Role();
        role.setId(userLoginResult.getUserId());
        role.setUserName(userLoginResult.getUserName());
        role.setBlood(userLoginResult.getHp());
        role.setAddressName(userLoginResult.getAddressName());
        role.setX(userLoginResult.getX());
        role.setY(userLoginResult.getY());

        starting(role);
    }

    public void starting(Role role){

        // 构建角色，角色初始在启始之地
        log.info("============欢迎勇士来到冒险大陆==============");

        operation(role);
    }

    public void operation(Role role) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String curScene = role.getAddressName();
            log.info("============所在地: {} ==============", curScene);
            log.info("============当前坐标:( {} , {} )==============", role.getX(), role.getY());

//            List<Npc> npcList = NpcDataListener.map.get(curScene);
//            log.info("============当前场景的npc个数: {}", (npcList == null ? 0 : npcList.size()));

//            if (npcList != null) {
//                for (int i = 0; i < npcList.size(); i++) {
//                    log.info("===>> {} 坐标: ( {} , {} )",npcList.get(i).getName(),npcList.get(i).getX(),npcList.get(i).getY());
//                }
//            }

            log.info("请输入您的操作: ");
            String command = scanner.nextLine();

            // 移动到的地址
            String address = command.substring(4);
            if ("mov:村子".equalsIgnoreCase(command) && "启始之地、森林、城堡".contains(curScene)) {
                moveScene(role, address);
            } else if ("mov:启始之地".equalsIgnoreCase(command) && curScene.contains("村子")) {
                moveScene(role, address);
            } else if ("mov:森林".equalsIgnoreCase(command) && curScene.contains("村子")) {
                moveScene(role, address);
            } else if ("mov:城堡".equalsIgnoreCase(command) && curScene.contains("村子")) {
                moveScene(role, address);
            } else if(!"mov:".equalsIgnoreCase(command.substring(0,4))){
                log.error("命令输入错误: {}",command.substring(0,4));
            }else {
                log.info("{} 和 {} 不相邻!",address ,curScene);
            }

        }
    }

    private void moveScene(Role role, String address) {
        role.setX(0);
        role.setY(0);
        role.setAddressName(address);
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


}
