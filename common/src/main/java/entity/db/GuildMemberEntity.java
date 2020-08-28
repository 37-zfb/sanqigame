package entity.db;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 * 公会成员实体
 */
@Data
@NoArgsConstructor
public class GuildMemberEntity {

    /**
     * id
     */
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 公会id
     */
    private Integer guildId;

    /**
     * 公会职位
     */
    private Integer guildPosition;

    /**
     *  用户名称
     */
    private String userName;

    /**
     *  是否在线
     */
    private boolean isOnline = false;

}
