package server.cmdhandler.auction;

import entity.db.CurrUserStateEntity;
import entity.db.DbAuctionItemEntity;
import entity.db.DbBidderEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.cmdhandler.mail.MailUtil;
import server.model.PlayAuction;
import server.model.User;
import server.timer.auction.DbAuctionTimer;
import server.timer.state.DbUserStateTimer;
import util.MyUtil;

import java.util.ArrayList;

/**
 * @author 张丰博
 * <p>
 * 竞价拍卖品
 */
@Component
@Slf4j
public class BiddingGoodsCmdHandler implements ICmdHandler<GameMsg.BiddingGoodsCmd> {

    @Autowired
    private DbAuctionTimer auctionTimer;
    @Autowired
    private DbUserStateTimer userStateTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.BiddingGoodsCmd biddingGoodsCmd) {

        MyUtil.checkIsNull(ctx, biddingGoodsCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        int money = biddingGoodsCmd.getMoney();
        int auctionId = biddingGoodsCmd.getAuctionId();

        DbAuctionItemEntity auctionItemById = PlayAuction.getAuctionItemById(auctionId);
        if (auctionItemById == null) {
            //拍卖品已卖出
            throw new CustomizeException(CustomizeErrorCode.ITEM_NOT_FOUNT);
        }
        if (user.getMoney() < money){
            throw new CustomizeException(CustomizeErrorCode.USER_MONEY_INSUFFICIENT);
        }

        DbBidderEntity dbBidderEntity = new DbBidderEntity();
        dbBidderEntity.setAuctionId(auctionId);
        dbBidderEntity.setMoney(money);
        dbBidderEntity.setUserId(user.getUserId());

        //添加竞拍者
        DbBidderEntity beforeBidder = auctionItemById.addBidder(dbBidderEntity);

        user.setMoney(user.getMoney() - money);

        CurrUserStateEntity userState = PublicMethod.getInstance().createUserState(user);
        userStateTimer.modifyUserState(userState);

        auctionTimer.addBidder(dbBidderEntity);
        if (beforeBidder != null) {

            MailUtil.getMailUtil().sendMail(beforeBidder.getUserId(),
                    beforeBidder.getMoney(),
                    "竞拍金币返回",
                    new ArrayList<>());
            auctionTimer.deleteBidder(beforeBidder);
        }


        log.info("用户 {} 参与竞拍 {} 拍卖物;", user.getUserName(), auctionId);
        GameMsg.BiddingGoodsResult biddingGoodsResult = GameMsg.BiddingGoodsResult.newBuilder()
                .setMoney(money)
                .build();
        ctx.writeAndFlush(biddingGoodsResult);
    }
}
