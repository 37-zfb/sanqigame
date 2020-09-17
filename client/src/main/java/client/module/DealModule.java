package client.module;

import client.model.Role;
import client.model.server.props.AbstractPropsProperty;
import client.model.server.props.Potion;
import client.model.server.props.Props;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import type.DealInfoType;
import type.PropsType;

import java.util.Map;
import java.util.Scanner;

/**
 * @author 张丰博
 */
@Slf4j
public class DealModule {
    private static final DealModule DEAL_THREAD = new DealModule();

    private DealModule() {
    }

    public static DealModule getInstance() {
        return DEAL_THREAD;
    }

    public void dealCmd(ChannelHandlerContext ctx, Role role) {

        System.out.println("金币: "+role.getMoney());

        Map<Integer, Props> backpackClient = role.getBackpackClient();
        for (Map.Entry<Integer, Props> propsEntry : backpackClient.entrySet()) {
            AbstractPropsProperty propsProperty = propsEntry.getValue().getPropsProperty();
            if (propsProperty.getType() == PropsType.Equipment){
                System.out.println(propsEntry.getKey() + " 、 " + propsEntry.getValue().getName());
            }else if (propsProperty.getType() == PropsType.Potion){
                Potion potion = (Potion) propsProperty;
                System.out.println(propsEntry.getKey() + " 、 " + propsEntry.getValue().getName()+ potion.getNumber());
            }
        }


        Scanner scanner = new Scanner(System.in);
        // 添加装备
        while (true) {
            System.out.println("1、添加装备;");
            System.out.println("2、减少装备;");
            System.out.println("99、交易;");
            System.out.println("999、确认交易;");
            System.out.println("88、取消;");
            System.out.println("888、取消确认交易;");
            System.out.println("111、切换页面;");
            String s = scanner.nextLine();
            if ("1".equals(s)) {
                addOrCancelProps(role, ctx, DealInfoType.ADD);
            } else if ("2".equals(s)) {
                addOrCancelProps(role, ctx, DealInfoType.CANCEL);
            }else if ("99".equals(s)){
                GameMsg.UserAddCompleteCmd userAddCompleteCmd = GameMsg.UserAddCompleteCmd.newBuilder().build();
                ctx.writeAndFlush(userAddCompleteCmd);
            }else if ("88".equals(s)){
                GameMsg.UserCancelDealCmd userCancelDealCmd = GameMsg.UserCancelDealCmd.newBuilder()
                        .setIsNeedNotice(true)
                        .build();
                ctx.writeAndFlush(userCancelDealCmd);
                break;
            }else if ("999".equals(s)){
                GameMsg.UserConfirmDealCmd userConfirmDealCmd = GameMsg.UserConfirmDealCmd.newBuilder().build();
                ctx.writeAndFlush(userConfirmDealCmd);
            }else if ("888".equals(s)){
                GameMsg.UserCancelDealConfirmCmd userCancelDealConfirmCmd =
                        GameMsg.UserCancelDealConfirmCmd.newBuilder()
                                .setIsNeedNotice(true)
                                .build();
                ctx.writeAndFlush(userCancelDealConfirmCmd);
            }else if ("111".equals(s)){
                break;
            }


        }
    }

    private void addOrCancelProps(Role role, ChannelHandlerContext ctx, DealInfoType dealInfoType) {
        Map<Integer, Props> backpackClient = role.getBackpackClient();
        Scanner scanner = new Scanner(System.in);

        System.out.println("重置金额;");
        int money = scanner.nextInt();
        scanner.nextLine();

        GameMsg.UserDealItemCmd userDealItemCmd = GameMsg.UserDealItemCmd.newBuilder()
                .setMoney(money)
                .setType(dealInfoType.getType())
                .build();
        ctx.writeAndFlush(userDealItemCmd);

        while (true) {
            System.out.println("0、退出;");
            int anInt = scanner.nextInt();
            scanner.nextLine();

            if (anInt == 0) {
                break;
            }

            Props props = backpackClient.get(anInt);

            GameMsg.Props.Builder newBuilder = GameMsg.Props.newBuilder()
                    .setLocation(anInt)
                    .setPropsId(props.getId())
                    .setPropsNumber(1);

            if (props.getPropsProperty().getType() != PropsType.Equipment) {
                System.out.println("数量:");
                int number = scanner.nextInt();
                newBuilder.setPropsNumber(number);
            }

            userDealItemCmd = GameMsg.UserDealItemCmd.newBuilder()
                    .setProps(newBuilder)
                    .setType(dealInfoType.getType())
                    .build();
            ctx.writeAndFlush(userDealItemCmd);
        }
    }
}
