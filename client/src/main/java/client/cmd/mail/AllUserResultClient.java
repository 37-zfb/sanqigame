package client.cmd.mail;

import client.model.server.props.Props;
import client.scene.GameData;
import client.CmdThread;
import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import type.PropsType;

import java.util.Map;
import java.util.Scanner;

/**
 * @author 张丰博
 */
public class AllUserResultClient implements ICmd<GameMsg.AllUserResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.AllUserResult allUserResult) {

        Role role = Role.getInstance();
        System.out.println("请选择用户");
        System.out.println("0、退出;");
        for (GameMsg.UserInfo userInfo : allUserResult.getUserInfoList()) {
            System.out.println(userInfo.getUserId() + "、" + userInfo.getUserName());
        }

        Scanner scanner = new Scanner(System.in);
        int userId = scanner.nextInt();
        scanner.nextLine();

        if (userId == 0) {
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
            return;
        }

        GameMsg.SendMailCmd.Builder newBuilder = GameMsg.SendMailCmd.newBuilder();
        //设置收件人id
        newBuilder.setTargetUserId(userId);

        System.out.println("邮件标题:");
        String title = scanner.next();
        scanner.nextLine();
        newBuilder.setTitle(title);

        System.out.println("请输入要邮寄的金币数量:");
        int money = scanner.nextInt();
        scanner.nextLine();
        newBuilder.setMoney(money);

        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        System.out.println("道具如下:");
        for (Props props : propsMap.values()) {
            System.out.println(props.getId() + "、" + props.getName());
        }


        while (true) {
            System.out.println("0、退出;");
            System.out.println("选择道具;");
            int propsId = scanner.nextInt();
            scanner.nextLine();
            if (propsId == 0) {
                break;
            }

            int number = 1;
            if (propsMap.get(propsId).getPropsProperty().getType() == PropsType.Potion) {
                System.out.println("数量;");
                number = scanner.nextInt();
                scanner.nextLine();
            }
            GameMsg.MailProps.Builder mailProps = GameMsg.MailProps.newBuilder()
                    .setPropsId(propsId)
                    .setNumber(number);
            newBuilder.addProps(mailProps);
        }

        newBuilder.setSrcUserId(0);

        GameMsg.SendMailCmd sendMailCmd = newBuilder.build();
        ctx.writeAndFlush(sendMailCmd);
        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
    }
}
