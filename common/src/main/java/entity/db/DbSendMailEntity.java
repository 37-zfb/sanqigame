package entity.db;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
public class DbSendMailEntity {

    /**
     * 本表id
     */
    private Long id;
    /**
     * 邮件标题
     */
    private String title;
    /**
     * 邮件要发送的用户id
     */
    private Integer targetUserId;
    /**
     * 发送者id
     */
    private Integer srcUserId;
    /**
     * 发送者名字
     */
    private String srcUserName;
    /**
     * 道具id
     */
    private String propsInfo;

    /**
     * 金币数
     */
    private Integer money;
    /**
     * 日期
     */
    private Date date;

    /**
     * 该邮件状态
     */
    private Integer state;
}
