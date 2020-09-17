package entity.db;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
public class DbGuildEquipment {
    /**
     * id
     */
    private Long id;

    /**
     * 仓库id
     */
    private Integer guildId;

    /**
     * 道具id
     */
    private Integer propsId;

    /**
     *  耐久度
     */
    private Integer durability;

    /**
     * 位置
     */
    private Integer location;

}
