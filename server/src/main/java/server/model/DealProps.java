package server.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author 张丰博
 */
@Setter
@Getter
@NoArgsConstructor
@ToString
public class DealProps {

    /**
     *  道具id
     */
    private Integer propsId;

    /**
     *  数量
     */
    private Integer number;

    public DealProps(Integer propsId,Integer number){
        this.propsId = propsId;
        this.number = number;
    }

}
