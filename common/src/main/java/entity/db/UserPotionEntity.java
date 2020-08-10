package entity.db;

import lombok.*;

/**
 * @author 张丰博
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserPotionEntity {

    private Integer id;

    private Integer userId;

    private Integer propsId;

    private Integer number;

    /**
     * 位置
     */
    private Integer location;

    public UserPotionEntity(Integer userId, Integer potionId, Integer number) {
        this.userId = userId;
        this.propsId = potionId;
        this.number = number;
    }

}
