package server.cmdhandler.auction;

import entity.db.DbAuctionItemEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayAuction;
import server.model.User;
import util.MyUtil;

import java.util.Collection;

/**
 * @author 张丰博
 * 查看自己上架物品
 */
@Component
@Slf4j
public class LookOneselfAuctionItemCmdHandler implements ICmdHandler<GameMsg.LookOneselfAuctionItemCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.LookOneselfAuctionItemCmd lookOneselfAuctionItemCmd) {

        MyUtil.checkIsNull(ctx, lookOneselfAuctionItemCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        GameMsg.LookOneselfAuctionItemResult.Builder newBuilder = GameMsg.LookOneselfAuctionItemResult.newBuilder();
        Collection<DbAuctionItemEntity> dbAuctionItemEntities = PlayAuction.listAuctionItem();
        for (DbAuctionItemEntity auctionItemEntity : dbAuctionItemEntities) {
            if (!auctionItemEntity.getUserId().equals(user.getUserId())){
                continue;
            }
            GameMsg.AuctionItem.Builder auctionInfo = GameMsg.AuctionItem.newBuilder()
                    .setId(auctionItemEntity.getId())
                    .setUserName(auctionItemEntity.getUserName())
                    .setPropsId(auctionItemEntity.getPropsId())
                    .setNumber(auctionItemEntity.getNumber())
                    .setAuction(auctionItemEntity.getAuction())
                    .setPrice(auctionItemEntity.getPrice())
                    .setDate(auctionItemEntity.getDate().getTime());
            newBuilder.addAuctionItem(auctionInfo);
        }

        log.info("用户 {} 查询上物品;", user.getUserName());
        GameMsg.LookOneselfAuctionItemResult lookOneselfAuctionItemResult = newBuilder.build();
        ctx.writeAndFlush(lookOneselfAuctionItemResult);
    }
}
