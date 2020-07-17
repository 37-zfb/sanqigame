package client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    private Integer id;

    private String userName;

    private Integer blood;

    private String addressName;

    private Integer x;

    private Integer y;

}
