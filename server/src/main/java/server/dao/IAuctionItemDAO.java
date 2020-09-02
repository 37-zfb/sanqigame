package server.dao;

import entity.db.DbAuctionItemEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author 张丰博
 */
@Repository
public interface IAuctionItemDAO {
    /**
     * 删除拍卖品
     * @param auctionItemCollection
     */
    void deleteAuctionItemBatch(@Param("auctionItemCollection") Collection<DbAuctionItemEntity> auctionItemCollection);

    /**
     * 添加拍卖品
     * @param auctionItemCollection
     */
    void insertAuctionItemBatch(@Param("auctionItemCollection") Collection<DbAuctionItemEntity> auctionItemCollection);

    /**
     * 修改拍卖品信息
     * @param auctionItemCollection
     */
    void updateAuctionItemBatch(@Param("auctionItemCollection") Collection<DbAuctionItemEntity> auctionItemCollection);

    /**
     * 查询所有 拍卖品
     * @return
     */
    List<DbAuctionItemEntity> selectAuctionItem();
}
