package server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    private Integer id;

    private String userName;

    private String password;
}
