package server.dao;

import entity.db.DbBidderEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author 张丰博
 */
@Repository
public interface IBidderDAO {
    /**
     * 删除
     * @param bidderCollection
     */
    void deleteBidderBatch(@Param("bidderCollection") Collection<DbBidderEntity> bidderCollection);

    /**
     * 添加拍卖品
     * @param bidderCollection
     */
    void insertBidderBatch(@Param("bidderCollection") Collection<DbBidderEntity> bidderCollection);

    /**
     * 通过拍卖物id查询所有竞拍者
     * @param auctionId
     * @return
     */
    List<DbBidderEntity> selectBidderByAuctionId(@Param("auctionId") Integer auctionId);

    /**
     * 修改拍卖品信息
     * @param bidderCollection
     */
   // void updateBidderBatch(@Param("bidderCollection") Collection<DbBidderEntity> bidderCollection);
}
