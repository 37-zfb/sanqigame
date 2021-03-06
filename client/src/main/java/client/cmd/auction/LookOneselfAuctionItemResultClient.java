package client.cmd.auction;

import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import client.model.server.props.Props;
import client.scene.GameData;
import client.CmdThread;
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
public class LookOneselfAuctionItemResultClient implements ICmd<GameMsg.LookOneselfAuctionItemResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.LookOneselfAuctionItemResult lookOneselfAuctionItemResult) {
        MyUtil.checkIsNull(ctx, lookOneselfAuctionItemResult);
        Role role = Role.getInstance();

        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        List<GameMsg.AuctionItem> auctionItemList = lookOneselfAuctionItemResult.getAuctionItemList();
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
                System.out.println(id + "、" + props.getName() + ", 竞拍:" + auction + ", 一口价:" + price + ", 到期时间:" + dateFormat.format(new Date(date)));
            } else if (props.getPropsProperty().getType() == PropsType.Potion) {
                System.out.println(id + "、" + props.getName() + " 数量: " + number + ", 竞拍:" + auction + ", 一口价:" + price + ", 到期时间:" + dateFormat.format(new Date(date)));
            }
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("0、退出");
        System.out.println("1、下架物品;");
        int anInt = scanner.nextInt();
        scanner.nextLine();
        if (1 == anInt){
            //下架
            System.out.println("选择物品;");
            int id = scanner.nextInt();
            scanner.nextLine();

            GameMsg.CancelAuctionItemCmd cancelAuctionItemCmd = GameMsg.CancelAuctionItemCmd.newBuilder()
                    .setAuctionId(id)
                    .build();
            ctx.writeAndFlush(cancelAuctionItemCmd);
        }

        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
    }
}
