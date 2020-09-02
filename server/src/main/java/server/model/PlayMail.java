package server.model;

import constant.MailConst;
import entity.db.DbSendMailEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.util.IdWorker;

import java.util.*;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
public class PlayMail {

    /**
     *  未读数
     */
    private Integer unreadNumber ;

    /**
     *  所有的邮件; 邮件id <=> 邮件基础类
     */
    private final Map<Long,DbSendMailEntity> mailEntityMap = new HashMap<>();

    /**
     * 添加邮件
     * @param mailEntity
     */
    public synchronized void addMail(DbSendMailEntity mailEntity){
        if (mailEntity == null){
            return;
        }
        long id = IdWorker.generateId();
        mailEntity.setId(id);
        mailEntityMap.put(id, mailEntity);
    }


}
