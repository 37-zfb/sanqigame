package entity.conf.props;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropsEntity {

    /**
     *  道具id
     */
    private Integer id;

    /**
     *  道具名称
     */
    private String name;
}
