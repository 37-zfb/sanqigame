package entity.db;

import lombok.*;

import java.util.Objects;

/**
 * @author 张丰博
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserEquipmentEntity {

    private Integer id;

    private Integer userId;

    /**
     * 充当 propsId
     */
    private Integer propsId;

    private Integer isWear;

    private Integer durability;

    /**
     * 位置
     */
    private Integer location;

    public UserEquipmentEntity(Integer id, Integer userId, Integer equipmentId, Integer isWear, Integer durability) {
        this.id = id;
        this.userId = userId;
        this.propsId = equipmentId;
        this.isWear = isWear;
        this.durability = durability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserEquipmentEntity that = (UserEquipmentEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(propsId, that.propsId) &&
                Objects.equals(isWear, that.isWear) &&
                Objects.equals(durability, that.durability);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, propsId, isWear, durability);
    }
}
