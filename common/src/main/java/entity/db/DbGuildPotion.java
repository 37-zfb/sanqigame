package entity.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DbGuildPotion {

    /**
     * id
     */
    private Integer id;
    /**
     * 公会id
     */
    private Integer guildId;
    /**
     *  道具id
     */
    private Integer propsId;

    /**
     * 数量
     */
    private Integer number;
    /**
     *  位置
     */
    private Integer location;

}
