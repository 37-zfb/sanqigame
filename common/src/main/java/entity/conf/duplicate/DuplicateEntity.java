package entity.conf.duplicate;

import lombok.*;

/**
 * @author 张丰博
 *
 * 副本
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateEntity {

    /**
     *  副本id
     */
    private Integer id;

    /**
     *  副本名称
     */
    private String name;

}
