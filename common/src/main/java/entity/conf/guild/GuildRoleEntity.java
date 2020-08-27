package entity.conf.guild;

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
public class GuildRoleEntity {

    private Integer id;

    private String role;

    public GuildRoleEntity(Integer id,String role){
        this.id = id;
        this.role = role;
    }

}
