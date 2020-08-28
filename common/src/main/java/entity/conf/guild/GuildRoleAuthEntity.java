package entity.conf.guild;

import lombok.Data;

/**
 * @author 张丰博
 */
@Data
public class GuildRoleAuthEntity {

    /**
     * 权限 id
     */
    private Integer id;
    /**
     * 角色id
     */
    private Integer roleId;
    /**
     * 权限
     */
    private String auth;
}
