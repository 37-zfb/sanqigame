package entity.conf.props;

import lombok.*;

/**
 * @author 张丰博
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PotionEntity {

    /**
     * id
     */
    private Integer id;


    /**
     * id
     */
    private Integer propsId;


    /**
     *  cd时间
     */
    private float cdTime;

    /**
     * 信息描述
     */
    private String info;

    /**
     *  恢复值
     */
    private Integer resumeFigure;

    /**
     *  恢复百分比
     */
    private float percent;

}
