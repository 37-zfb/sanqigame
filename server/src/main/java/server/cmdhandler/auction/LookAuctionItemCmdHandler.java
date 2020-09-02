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
 * 查询拍卖行商品
 */
@Component
@Slf4j
public class LookAuctionItemCmdHandler implements ICmdHandler<GameMsg.LookAuctionItemCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.LookAuctionItemCmd lookAuctionItemCmd) {

        MyUtil.checkIsNull(ctx, lookAuctionItemCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        GameMsg.LookAuctionItemResult.Builder newBuilder = GameMsg.LookAuctionItemResult.newBuilder();

        Collection<DbAuctionItemEntity> auctionItemCollection = PlayAuction.listAuctionItem();
        for (DbAuctionItemEntity auctionItemEntity : auctionItemCollection) {
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

        log.info("用户 {} 查询拍卖商品;", user.getUserName());

        GameMsg.LookAuctionItemResult lookAuctionItemResult = newBuilder.build();
        ctx.writeAndFlush(lookAuctionItemResult);
    }
}
