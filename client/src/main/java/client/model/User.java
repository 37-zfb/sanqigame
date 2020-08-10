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
public class User {
    private String userName;

    private String password;

    private Integer professionId;

    public User(String userName,String password){
        this.userName = userName;
        this.password = password;
    }

}
