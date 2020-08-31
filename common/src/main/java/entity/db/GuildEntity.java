package entity.db;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 * 公会实体
 */
@Data
@NoArgsConstructor
public class GuildEntity {
    /**
     *  id
     */
    private Integer id;

    /**
     * 公会名称
     */
    private String guildName;

    /**
     *  会长id
     */
    private Integer presidentId;

    /**
     *  公会金币
     */
    private Integer money;
}
