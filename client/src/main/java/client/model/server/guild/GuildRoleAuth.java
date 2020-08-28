package client.model.server.guild;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author 张丰博
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class GuildRoleAuth {
    /**
     *  id
     */
    private Integer id;
    /**
     *  角色id
     */
    private Integer roleId;

    /**
     *  权限
     */
    private String auth;

    public GuildRoleAuth(Integer id,Integer roleId,String auth){
        this.id = id;
        this.roleId = roleId;
        this.auth = auth;
    }

}
