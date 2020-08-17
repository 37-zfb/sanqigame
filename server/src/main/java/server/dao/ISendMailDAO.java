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
     *  查询十天内未读的邮件
     * @param userId 用户id
     * @param date 十天前的时间
     * @return
     */
    List<DbSendMailEntity> selectMailWithinTenDay(@Param("targetUserId") int userId, @Param("date") Date date);

    /**
     *  批量修改 状态值
     * @param mailCollection
     */
    void updateMailBatch(@Param("mailCollection") Collection<DbSendMailEntity> mailCollection);
}
