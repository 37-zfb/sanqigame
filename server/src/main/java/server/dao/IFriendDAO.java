package server.dao;

import entity.db.DbFriendEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author 张丰博
 */
@Repository
public interface IFriendDAO {

    /**
     * 批量添加好友
     * @param friendEntityList
     */
    void insertBatch(@Param("friendEntityList") List<DbFriendEntity> friendEntityList);

    /**
     * 批量删除好友
     * @param friendEntityList
     */
    void deleteBatch(@Param("friendEntityList") List<DbFriendEntity> friendEntityList);

    /**
     * 通关用户id查询好友
     * @param userId
     * @return
     */
    List<DbFriendEntity> selectByUserId(@Param("userId") Integer userId);
}
