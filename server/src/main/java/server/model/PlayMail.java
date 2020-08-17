package server.model;

import entity.db.DbSendMailEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final Map<Integer,DbSendMailEntity> mailEntityMap = new HashMap<>();

}
