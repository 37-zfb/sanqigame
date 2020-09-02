package server.dao;

import entity.db.DbSendMailEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author 张丰博
 */
@Repository
public interface ISendMailDAO {

    /**
     *  添加邮件信息
     * @param mailEntity
     */
    void insertMail(DbSendMailEntity mailEntity);


    /**
     *  查询未读的邮件
     * @param userId
     * @return
     */
    List<DbSendMailEntity> selectMailUnread(@Param("targetUserId") int userId);


    /**
     *  批量修改 状态值
     * @param mailCollection
     */
    void updateMailBatch(@Param("mailCollection") Collection<DbSendMailEntity> mailCollection);

    /**
     * 查询是否存在此条信息
     * @param userId
     * @param title
     * @return
     */
    DbSendMailEntity selectMailByUserIdAndTitle(@Param("targetUserId") int userId,@Param("title") String title);

    /**
     * 批量添加邮件信息
     * @param addMailList
     */
    void insertMailBatch(@Param("addMailList") List<DbSendMailEntity> addMailList);
}
