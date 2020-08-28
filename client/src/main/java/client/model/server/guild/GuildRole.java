package client.model.server.guild;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author 张丰博
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
public class GuildRole {
    /**
     * id
     */
    private Integer id;

    /**
     * 角色
     */
    private String roleName;

    /**
     *  权限
     */
    private GuildRoleAuth guildRoleAuth;

    public GuildRole(Integer id,String roleName){
        this.id = id;
        this.roleName = roleName;
    }


}
