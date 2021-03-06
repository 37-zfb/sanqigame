package client.cmd;

import client.model.server.scene.Monster;
import client.model.server.scene.Npc;
import client.model.server.scene.Scene;
import client.CmdThread;
import client.model.Role;
import client.model.SceneData;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import type.ChatType;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author 张丰博
 */
@Slf4j
public class WhoElseIsHereCmdClient implements ICmd<GameMsg.WhoElseIsHereResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.WhoElseIsHereResult result) {

        if (ctx == null || result == null) {
            return;
        }

        Role role = Role.getInstance();
        Scene scene = SceneData.getInstance().getSceneMap().get(role.getCurrSceneId());

        // 保存服务的响应的
        // 1、用户信息
        List<GameMsg.UserInfo> userInfoList = result.getUserInfoList();
        System.out.println("============当前场景的用户个数:  " + userInfoList.size());
        for (GameMsg.UserInfo userInfo : userInfoList) {
            // 获得用户信息
            System.out.println("============用户id:" + userInfo.getUserId() + "、 名字: " + userInfo.getUserName() + " 组队: " + (userInfo.getIsTeam() ? "是" : "无"));
        }

        Scanner scanner = new Scanner(System.in);

        if (role.isAddFriend()) {
            role.setAddFriend(false);
            //添加好友
            System.out.println("0、退出;");
            System.out.println("1、添加好友;");
            int anInt = scanner.nextInt();
            scanner.nextLine();
            if (anInt == 1) {
                System.out.println("输入用户;");
                int userId = scanner.nextInt();
                scanner.nextLine();

                String userName = "";
                for (GameMsg.UserInfo userInfo : userInfoList) {
                    if (userInfo.getUserId() == userId) {
                        userName = userInfo.getUserName();
                    }
                }

                GameMsg.AddFriendCmd friendCmd = GameMsg.AddFriendCmd.newBuilder()
                        .setUserId(userId)
                        .setUserName(userName)
                        .build();
                ctx.writeAndFlush(friendCmd);
            }


        } else if (role.isChat()) {
            // 聊天
            System.out.println("选择用户: ");
            int userId = scanner.nextInt();
            scanner.nextLine();

            // 对话内容
            System.out.println("对话内容:");
            String info = scanner.nextLine();

            role.setChat(false);

            GameMsg.UserChatInfoCmd userChatInfoCmd = GameMsg.UserChatInfoCmd.newBuilder()
                    .setTargetUserId(userId)
                    .setInfo(info)
                    .setType(ChatType.PRIVATE_CHAT.getChatType())
                    .build();
            ctx.writeAndFlush(userChatInfoCmd);
        } else if (role.isTeam()) {
            // 组队
            System.out.println("选择用户: ");
            System.out.println("0、退出;");
            int userId = scanner.nextInt();

            role.setTeam(false);

            if (0 != userId) {
                GameMsg.UserTeamUpCmd userTeamUpCmd = GameMsg.UserTeamUpCmd.newBuilder()
                        .setTargetUserId(userId)
                        .build();
                ctx.writeAndFlush(userTeamUpCmd);
            }


        } else if (role.isDeal()) {
            // 交易
            System.out.println("选择用户: ");
            System.out.println("0、退出;");
            int userId = scanner.nextInt();

            role.setDeal(false);

            if (0 != userId){
                GameMsg.UserDealRequestCmd userDealRequestCmd = GameMsg.UserDealRequestCmd.newBuilder()
                        .setUserId(userId)
                        .build();
                ctx.writeAndFlush(userDealRequestCmd);
            }

        } else {
            // 2、npc信息
            Map<Integer, Npc> npcMap = scene.getNpcMap();
            System.out.println("============当前场景的Npc个数:  " + npcMap.size());
            for (Npc npc : npcMap.values()) {
                System.out.println("============Npc:  " + npc.getName());
            }

            // 3、怪信息
            Map<Integer, Monster> monsterMap = scene.getMonsterMap();
            System.out.println("============当前场景的怪个数:  " + monsterMap.size());
            for (Monster monster : monsterMap.values()) {
                System.out.println("============怪:  " + monster.getName() + " ," + " 状态: " + (monster.isDie() ? "已被击杀" : "存活") + " , 血量: " + monster.getHp());
            }
        }


        CmdThread.getInstance().process(ctx, role, scene.getNpcMap().values());

    }
}
