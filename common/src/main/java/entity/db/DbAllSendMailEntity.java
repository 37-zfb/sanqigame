package entity.db;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author 张丰博
 *
 * 数据库 全体邮件 实体类
 */
@Data
@NoArgsConstructor
public class DbAllSendMailEntity {

    /**
     * id
     */
    private Long id;

    /**
     * 邮件标题
     */
    private String title;

    /**
     * 道具 json
     */
    private String propsInfo;

    /**
     * 钱
     */
    private Integer money;

    /**
     * 日期
     */
    private Date date;

    /**
     * 来源
     */
    private String srcUserName;

}
