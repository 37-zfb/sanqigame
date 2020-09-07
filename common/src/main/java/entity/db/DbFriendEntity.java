package entity.db;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
public class DbFriendEntity {

    /**
     * id
     */
    private Long id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 朋友id
     */
    private Integer friendId;

    /**
     * 朋友名字
     */
    private String friendName;
}
