package server.service;

import entity.db.UserBuyGoodsLimitEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.dao.IUserGoodsLimitDAO;
import server.model.User;
import server.model.store.Goods;
import util.CustomizeThreadFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

/**
 * @author 张丰博
 */
@Service
public class StoreService {

    @Autowired
    private IUserGoodsLimitDAO goodsLimitDAO;


    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("限购商品持久化数据库;")
    );


    /**
     * 延迟更新商品数量
     * @param user
     * @param goods
     */
    public void modifyGoodsBuyNumber(User user, Goods goods){
        if (user == null || goods == null){
            return;
        }
        UserBuyGoodsLimitEntity userBuyGoodsLimitEntity = createEntity(user, goods);
        scheduledThreadPool.schedule(()->{
            goodsLimitDAO.updateLimitNumber(userBuyGoodsLimitEntity);
        }, 2, TimeUnit.SECONDS);
    }


    /**
     * 立即添加已购买商品数量
     * @param user
     * @param goods
     */
    public void addGoodsBuyNumber(User user, Goods goods) {
        if (user == null || goods == null){
            return;
        }

        UserBuyGoodsLimitEntity userBuyGoodsLimitEntity = createEntity(user, goods);
        scheduledThreadPool.schedule(()->{
            goodsLimitDAO.insertEntity(userBuyGoodsLimitEntity);
        },0,TimeUnit.SECONDS);
    }



    private UserBuyGoodsLimitEntity createEntity(User user, Goods goods) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(new Date());

        // 当前商品允许买的个数
        Integer allowBuyNumber = user.getGoodsAllowNumber().get(goods.getId());

        UserBuyGoodsLimitEntity userBuyGoodsLimitEntity = new UserBuyGoodsLimitEntity();
        userBuyGoodsLimitEntity.setUserId(user.getUserId());
        userBuyGoodsLimitEntity.setGoodsId(goods.getId());
        userBuyGoodsLimitEntity.setDate(date);
        userBuyGoodsLimitEntity.setNumber(goods.getNumberLimit() - allowBuyNumber);

        return userBuyGoodsLimitEntity;
    }
}
