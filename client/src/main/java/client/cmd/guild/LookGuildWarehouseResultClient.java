package client.cmd.guild;

import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import client.model.server.props.AbstractPropsProperty;
import client.model.server.props.Equipment;
import client.model.server.props.Potion;
import client.model.server.props.Props;
import client.scene.GameData;
import client.CmdThread;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import type.PropsType;
import util.MyUtil;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author 张丰博
 */
public class LookGuildWarehouseResultClient implements ICmd<GameMsg.LookGuildWarehouseResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.LookGuildWarehouseResult lookGuildWarehouseResult) {

        MyUtil.checkIsNull(ctx, lookGuildWarehouseResult);
        Role role = Role.getInstance();

        int money = lookGuildWarehouseResult.getMoney();
        System.out.println("仓库金币: " + money);
        System.out.println("仓库道具: ");
        List<GameMsg.Props> propsList = lookGuildWarehouseResult.getPropsList();
        for (GameMsg.Props props : propsList) {
            int propsId = props.getPropsId();
            Props p = GameData.getInstance().getPropsMap().get(propsId);
            System.out.println(props.getUserPropsId() + "、" + p.getName() + " " + props.getPropsNumber());
        }

        System.out.println("0、退出;");
        System.out.println("1、放入金币;");
        System.out.println("2、放入道具;");
        System.out.println("3、取出金币;");
        System.out.println("4、取出道具;");

        Scanner scanner = new Scanner(System.in);
        int anInt = scanner.nextInt();
        scanner.nextLine();
        if (0 == anInt) {
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        } else if (1 == anInt) {
            System.out.println("放入金币数: ");
            int putInMoney = scanner.nextInt();
            scanner.nextLine();

//            role.setMoney(role.getMoney() - putInMoney);

            GameMsg.PutInMoneyCmd newBuilder = GameMsg.PutInMoneyCmd.newBuilder()
                    .setMoney(putInMoney)
                    .build();
            ctx.writeAndFlush(newBuilder);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        } else if (2 == anInt) {
            System.out.println("放入道具;");
            Map<Integer, Props> backpackClient = role.getBackpackClient();
            for (Map.Entry<Integer, Props> propsEntry : backpackClient.entrySet()) {
                AbstractPropsProperty propsProperty = propsEntry.getValue().getPropsProperty();
                if (propsProperty.getType() == PropsType.Equipment) {
                    Equipment equipment = (Equipment) propsProperty;
                    System.out.println(propsEntry.getKey() + "、" + propsEntry.getValue().getName() + "、" + equipment.getDurability());
                } else if (propsProperty.getType() == PropsType.Potion) {
                    Potion potion = (Potion) propsProperty;
                    System.out.println(propsEntry.getKey() + "、" + propsEntry.getValue().getName() + "、" + potion.getNumber());
                }
            }

            System.out.println("0、退出;");
            int i = scanner.nextInt();
            scanner.nextLine();
            if (i == 0) {
                CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
            }
            Equipment equipment = null;
            Potion potion = null;
            int number = 0;

            GameMsg.PutInPropsCmd.Builder builder = GameMsg.PutInPropsCmd.newBuilder();
            GameMsg.Props.Builder propsBuilder = GameMsg.Props.newBuilder();
            Props props = backpackClient.get(i);
            AbstractPropsProperty propsProperty = props.getPropsProperty();
            if (propsProperty.getType() == PropsType.Equipment) {
                equipment = (Equipment) propsProperty;
                propsBuilder.setLocation(i)
                        .setPropsId(equipment.getPropsId())
                        .setDurability(equipment.getDurability())
                        .setPropsNumber(1);
            } else if (propsProperty.getType() == PropsType.Potion) {
                System.out.println("数量:");
                number = scanner.nextInt();
                scanner.nextLine();

                potion = (Potion) propsProperty;
                propsBuilder.setLocation(i)
                        .setPropsId(potion.getPropsId())
                        .setPropsNumber(number);
            }
            builder.setProps(propsBuilder);
            GameMsg.PutInPropsCmd putInPropsCmd = builder.build();
            ctx.writeAndFlush(putInPropsCmd);

//            if (equipment != null) {
//                backpackClient.remove(i);
//            } else if (potion != null) {
//                if (potion.getNumber() > number){
//                    potion.setNumber(potion.getNumber()-number);
//                }else {
//                    backpackClient.remove(i);
//                }
//
//            }

            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());

        } else if (3 == anInt) {

            System.out.println("取出金币: ");
            int takeOutMoney = scanner.nextInt();
            scanner.nextLine();

//            role.setMoney(role.getMoney() + takeOutMoney);

            GameMsg.TakeOutMoneyCmd takeOutMoneyCmd = GameMsg.TakeOutMoneyCmd
                    .newBuilder()
                    .setMoney(takeOutMoney)
                    .build();

            ctx.writeAndFlush(takeOutMoneyCmd);

            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());

        } else if (4 == anInt) {
            System.out.println("取出装备;");
            System.out.println("0、退出;");
            int i = scanner.nextInt();
            scanner.nextLine();
            if (i == 0) {
                CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
            }

            GameMsg.TakeOutPropsCmd.Builder newBuilder = GameMsg.TakeOutPropsCmd.newBuilder();

            GameMsg.Props p = null;
            for (GameMsg.Props props : propsList) {
                if (props.getUserPropsId() == i) {
                    newBuilder.setProps(props);
                    p = props;
                    break;
                }
            }

            int number = 1;
            if (p != null && p.getPropsNumber() > 1) {
                //此时是药剂
                System.out.println("药剂数量:");
                number = scanner.nextInt();
                scanner.nextLine();
                newBuilder.setNumber(number);
            }


            GameMsg.TakeOutPropsCmd takeOutCmd = newBuilder.build();
            ctx.writeAndFlush(takeOutCmd);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        }


    }


}

