package server.cmdhandler.auction;

import com.alibaba.fastjson.JSON;
import entity.db.DbAuctionItemEntity;
import entity.db.DbBidderEntity;
import entity.db.DbSendMailEntity;
import msg.GameMsg;
import server.GameServer;
import entity.MailProps;
import server.model.User;
import server.model.UserManager;
import server.timer.auction.DbAuctionTimer;
import server.timer.mail.DbSendMailTimer;
import server.util.IdWorker;
import type.MailType;

import java.util.*;

/**
 * @author 张丰博
 * 拍卖行工具类
 */
public final class AuctionUtil {

    private static final DbSendMailTimer sendMailTimer = GameServer.APPLICATION_CONTEXT.getBean(DbSendMailTimer.class);

    private static final DbAuctionTimer auctionTimer = GameServer.APPLICATION_CONTEXT.getBean(DbAuctionTimer.class);

    private AuctionUtil(){}


    /**
     * 给拍卖中发邮件
     * @param userId
     * @param money
     */
    public static void sendMoneyMail(Integer userId, Integer money, String title){
        //发送邮件
        DbSendMailEntity dbSendMailEntity = new DbSendMailEntity();
        dbSendMailEntity.setId(IdWorker.generateId());
        dbSendMailEntity.setTargetUserId(userId);
        dbSendMailEntity.setSrcUserId(0);
        dbSendMailEntity.setMoney(money);
        dbSendMailEntity.setState(MailType.UNREAD.getState());
        dbSendMailEntity.setDate(new Date());
        dbSendMailEntity.setTitle(title);
        dbSendMailEntity.setSrcUserName("管理员");

        List<MailProps> list = new ArrayList<>();
        String propsInfo = JSON.toJSONString(list);

        dbSendMailEntity.setPropsInfo(propsInfo);
        sendMailTimer.addMailList(dbSendMailEntity);

        User user = UserManager.getUserById(userId);
        if (user!=null){
            send(dbSendMailEntity, user);
        }

    }

    /**
     * 发送给购买者邮件
     * @param userId
     * @param propsId
     * @param number
     */
    public static void sendPropsMail(Integer userId, Integer propsId, Integer number, String title){
        //发送邮件
        DbSendMailEntity dbSendMailEntity = new DbSendMailEntity();
        dbSendMailEntity.setId(IdWorker.generateId());
        dbSendMailEntity.setTargetUserId(userId);
        dbSendMailEntity.setSrcUserId(0);
        dbSendMailEntity.setMoney(0);
        dbSendMailEntity.setState(MailType.UNREAD.getState());
        dbSendMailEntity.setDate(new Date());
        dbSendMailEntity.setTitle(title);
        dbSendMailEntity.setSrcUserName("管理员");

        List<MailProps> list = new ArrayList<>();
        list.add(new MailProps(propsId, number));

        String propsInfo = JSON.toJSONString(list);
        dbSendMailEntity.setPropsInfo(propsInfo);
        sendMailTimer.addMailList(dbSendMailEntity);

        User user = UserManager.getUserById(userId);
        if (user!=null){
            send(dbSendMailEntity,user);
        }
    }

    /**
     * 发送邮件
     * @param dbSendMailEntity
     * @param user
     */
    private static void send(DbSendMailEntity dbSendMailEntity,User user){
        //用户添加邮件
        user.getMail().addMail(dbSendMailEntity);

        GameMsg.MailInfo.Builder mailInfoBuilder = GameMsg.MailInfo.newBuilder()
                .setTitle(dbSendMailEntity.getTitle())
                .setMailId(dbSendMailEntity.getId())
                .setSrcUserName(dbSendMailEntity.getSrcUserName());

        GameMsg.NoticeUserGetMailResult getMailResult = GameMsg.NoticeUserGetMailResult
                .newBuilder()
                .setMailInfo(mailInfoBuilder)
                .build();
        user.getCtx().writeAndFlush(getMailResult);
    }

    /**
     * 当物品上架时间到时，计算是否有竞拍者，有则把物品给最高价者
     * @param auctionItemEntity
     * @param dbBidderEntity
     */
    public static void auctionResult(DbAuctionItemEntity auctionItemEntity, DbBidderEntity dbBidderEntity) {
        //下架时间到
        if (dbBidderEntity != null) {
            //出价最高者
            AuctionUtil.sendPropsMail(dbBidderEntity.getUserId(), auctionItemEntity.getPropsId(), auctionItemEntity.getNumber(),"竞拍成功,获得拍卖品");
            //删除竞拍者
            auctionTimer.deleteBidder(dbBidderEntity);
            //把钱发给拍卖者
            AuctionUtil.sendMoneyMail(auctionItemEntity.getUserId(), dbBidderEntity.getMoney(), "拍卖物品卖出金币");
        }else {
            // 若没有竞拍者，此时把拍卖品返回拍卖者
            AuctionUtil.sendPropsMail(auctionItemEntity.getUserId(),auctionItemEntity.getPropsId(),auctionItemEntity.getNumber(),"拍卖失败,返回拍卖品");
        }
    }

}
