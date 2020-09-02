package client.cmd.auction;

import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import client.model.server.props.Props;
import client.scene.GameData;
import client.thread.CmdThread;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import type.PropsType;
import util.MyUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author 张丰博
 */
public class LookAuctionItemResultClient implements ICmd<GameMsg.LookAuctionItemResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.LookAuctionItemResult lookAuctionItemResult) {

        MyUtil.checkIsNull(ctx, lookAuctionItemResult);
        Role role = Role.getInstance();

        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();

        List<GameMsg.AuctionItem> auctionItemList = lookAuctionItemResult.getAuctionItemList();
        for (GameMsg.AuctionItem auctionItem : auctionItemList) {
            Props props = propsMap.get(auctionItem.getPropsId());

            int id = auctionItem.getId();
            String userName = auctionItem.getUserName();
            int auction = auctionItem.getAuction();
            int price = auctionItem.getPrice();
            int number = auctionItem.getNumber();
            long date = auctionItem.getDate();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            if (props.getPropsProperty().getType() == PropsType.Equipment) {
                System.out.println(id + "、" + props.getName() + ",上架人: " + userName + ", 竞拍:" + auction + ", 一口价:" + price + ", 到期时间:" + dateFormat.format(new Date(date)));
            } else if (props.getPropsProperty().getType() == PropsType.Potion) {
                System.out.println(id + "、" + props.getName() + " 数量: " + number + ",上架人: " + userName + ", 竞拍:" + auction + ", 一口价:" + price + ", 到期时间:" + dateFormat.format(new Date(date)));
            }
        }

        System.out.println("0、退出;");
        System.out.println("请选择拍卖品;");

        Scanner scanner = new Scanner(System.in);
        int id = scanner.nextInt();
        scanner.nextLine();

        if (0 == id) {
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
            return;
        }

        System.out.println("1、竞价;");
        System.out.println("2、一口价;");

        int anInt = scanner.nextInt();
        scanner.nextLine();

        if (1 == anInt) {
            System.out.println("输入金币数:");
            int money = scanner.nextInt();
            scanner.nextLine();

            role.setMoney(role.getMoney() - money);

            ctx.writeAndFlush(GameMsg.BiddingGoodsCmd.newBuilder()
                    .setMoney(money)
                    .setAuctionId(id)
                    .build());
        } else if (2 == anInt) {

            GameMsg.AuctionItem auction = null;
            for (GameMsg.AuctionItem auctionItem : auctionItemList) {
                if (auctionItem.getId() == id) {
                    auction = auctionItem;
                    break;
                }
            }

            int number = auction.getNumber();
            role.setMoney(role.getMoney() - auction.getPrice());

//            //从拍卖行买到装备
//            Map<Integer, Props> backpackClient = role.getBackpackClient();
//            Props pro = propsMap.get(auction.getPropsId());
//            if (pro.getPropsProperty().getType() == PropsType.Equipment) {
//                Equipment equipment = (Equipment) pro.getPropsProperty();
//
//                Equipment equ = null;
//                for (int j = 1; j < BackPackConst.MAX_CAPACITY; j++) {
//                    if (!backpackClient.keySet().contains(j)) {
//                        Props addPro = new Props();
//                        addPro.setId(equipment.getPropsId());
//                        addPro.setName(pro.getName());
//                        equ = new Equipment(null, pro.getId(), EquipmentConst.MAX_DURABILITY, equipment.getDamage(), equipment.getEquipmentType());
//                        pro.setPropsProperty(equ);
//
//                        backpackClient.put(j, pro);
//                        break;
//                    }
//                }
//            } else if (pro.getPropsProperty().getType() == PropsType.Potion) {
//                Potion potion = (Potion) pro.getPropsProperty();
//
//                boolean isExist = false;
//                for (Props existPro : backpackClient.values()) {
//                    // 查询背包中是否有该药剂
//                    if (potion.getPropsId().equals(existPro.getId())) {
//                        // 判断该药剂的数量是否达到上限
//                        // 背包中已有该药剂
//                        Potion po = (Potion) existPro.getPropsProperty();
//                        if ((po.getNumber() + number) > PotionConst.POTION_MAX_NUMBER) {
//                            throw new CustomizeException(CustomizeErrorCode.PROPS_REACH_LIMIT);
//                        }
//
//                        po.setNumber(po.getNumber() + number);
//                        isExist = true;
//                        break;
//                    }
//                }
//                // 背包中还没有该药剂
//                Potion po = null;
//                if (!isExist) {
//                    if (backpackClient.size() >= BackPackConst.MAX_CAPACITY) {
//                        // 此时背包已满，
//                        throw new CustomizeException(CustomizeErrorCode.BACKPACK_SPACE_INSUFFICIENT);
//                    }
//
//                    for (int j = 1; j < BackPackConst.MAX_CAPACITY; j++) {
//                        if (!backpackClient.keySet().contains(j)) {
//                            Props addPro = new Props();
//                            addPro.setId(potion.getPropsId());
//                            addPro.setName(pro.getName());
//                            po = new Potion(null, potion.getPropsId(), potion.getCdTime(), potion.getInfo(), potion.getResumeFigure(), potion.getPercent(), number);
//                            addPro.setPropsProperty(po);
//
//                            // 药剂添加进背包
//                            backpackClient.put(j, pro);
//                            break;
//                        }
//                    }
//                }
//
//            }
//
//
            ctx.writeAndFlush(GameMsg.OnePriceCmd.newBuilder()
                    .setAuctionId(id)
                    .build());
        }

        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());

    }
}

