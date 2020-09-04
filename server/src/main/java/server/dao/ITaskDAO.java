package server.dao;

import entity.db.DbTaskEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author 张丰博
 */
@Repository
public interface ITaskDAO {

    /**
     * 添加数据
     * @param taskEntity
     */
    void insert(DbTaskEntity taskEntity);

    /**
     * 删除
     * @param taskEntity
     */
    void delete(DbTaskEntity taskEntity);

    /**
     * 更新
     * @param taskEntity
     */
    void update(DbTaskEntity taskEntity);

    /**
     * 通过userId查询
     * @param userId
     * @return
     */
    DbTaskEntity selectByUserId(@Param("userId") Integer userId);

    /**
     * 查询所有
     * @return
     */
    List<DbTaskEntity> select();
}
