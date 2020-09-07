package server.service;

import entity.db.DbAuctionItemEntity;
import entity.db.DbBidderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.dao.IAuctionItemDAO;
import server.dao.IBidderDAO;

import java.util.Collection;
import java.util.List;

/**
 * @author 张丰博
 * 拍卖品
 */
@Service
@Slf4j
public class AuctionService {

    @Autowired
    private IAuctionItemDAO auctionItemDAO;

    @Autowired
    private IBidderDAO bidderDAO;

    /**
     * 批量删除拍卖品
     *
     * @param auctionItemCollection
     */
    public void deleteAuctionItemBatch(Collection<DbAuctionItemEntity> auctionItemCollection) {
        if (auctionItemCollection != null) {
            auctionItemDAO.deleteAuctionItemBatch(auctionItemCollection);
        }
    }

    /**
     * 批量修改拍卖品信息
     *
     * @param auctionItemCollection
     */
    public void modifyAuctionItemBatch(Collection<DbAuctionItemEntity> auctionItemCollection) {
        if (auctionItemCollection != null) {
            auctionItemDAO.updateAuctionItemBatch(auctionItemCollection);
        }
    }

    /**
     * 批量添加拍卖品
     *
     * @param auctionItemCollection
     */
    public void addAuctionItemBatch(Collection<DbAuctionItemEntity> auctionItemCollection) {
        if (auctionItemCollection != null) {
            auctionItemDAO.insertAuctionItemBatch(auctionItemCollection);
        }
    }

    /**
     * 添加竞拍者
     *
     * @param addBidderList
     */
    public void addBidderBatch(List<DbBidderEntity> addBidderList) {
        if (addBidderList != null) {
            bidderDAO.insertBidderBatch(addBidderList);
        }
    }

    /**
     * 修改竞拍者
     * @param modifyBidderList
     */
    public void modifyBidderBatch(List<DbBidderEntity> modifyBidderList) {
        if (modifyBidderList != null) {
        }
    }

    /**
     * 删除竞拍者
     *
     * @param deleteBidderList
     */
    public void deleteBidderBatch(List<DbBidderEntity> deleteBidderList) {
        if (deleteBidderList != null) {
            bidderDAO.deleteBidderBatch(deleteBidderList);
        }
    }

    /**
     * 查询所有 拍卖品
     * @return
     */
    public List<DbAuctionItemEntity> listAuctionItem() {

        return auctionItemDAO.selectAuctionItem();
    }

    /**
     *
     * @return
     */
    public List<DbBidderEntity> listBidder(Integer auctionId) {
        return bidderDAO.selectBidderByAuctionId(auctionId);
    }

    public void deleteAuctionItem(DbAuctionItemEntity auctionItemEntity) {
        auctionItemDAO.deleteAuction(auctionItemEntity);
    }
}
