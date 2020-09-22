package server.dao;

import entity.db.DbAllSendMailEntity;
import entity.db.DbSendMailEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author 张丰博
 */
@Repository
public interface IAllSendMailDAO {

    /**
     *  添加邮件信息
     * @param mailEntity
     */
    void insertMail(DbAllSendMailEntity mailEntity);


    /**
     *  查询所有的邮件
     * @return
     */
    List<DbAllSendMailEntity> selectMail();


    /**
     * 删除邮件信息
     * @param id
     */
    void deleteMail(@Param("id") Integer id);
}
