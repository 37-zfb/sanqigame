package entity.db;

import lombok.*;

import java.util.Objects;

/**
 * @author 张丰博
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserPotionEntity {

    private Long id;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPotionEntity that = (UserPotionEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(propsId, that.propsId) &&
                Objects.equals(number, that.number) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, propsId, number, location);
    }
}
