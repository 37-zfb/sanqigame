package model.props;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 张丰博
 */
@Getter
@Setter
public class Props {

    /**
     * id
     */
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     *  道具属性
     */
    private AbstractPropsProperty propsProperty;

    public Props(){}

    public Props(Integer id,String name){
        this.id = id;
        this.name = name;
    }
    public Props(Integer id,String name,AbstractPropsProperty propsProperty){
        this.id = id;
        this.name = name;
        this.propsProperty = propsProperty;
    }
}
