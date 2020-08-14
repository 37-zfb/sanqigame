package model.props;

import lombok.Getter;
import lombok.Setter;
import type.PropsType;

/**
 * @author 张丰博
 */
@Setter
@Getter
public abstract class AbstractPropsProperty {

    /**
     *  数据库中 userequipment_id
     */
    private Integer id;

    /**
     *  道具id
     */
    private Integer propsId;

    public AbstractPropsProperty(){}

    public AbstractPropsProperty(Integer id,Integer propsId){
        this.id = id;
        this.propsId = propsId;
    }

    /**
     *  获取当前对象类型
     * @return
     */
    public abstract PropsType getType();

    /**
     *  是否限购
     * @return
     */
    public abstract PropsType isLimit();
}
