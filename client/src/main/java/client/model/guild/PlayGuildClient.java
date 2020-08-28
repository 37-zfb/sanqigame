package client.model.guild;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
public class PlayGuildClient {

    /**
     * 公会职位
     */
    private String type;

    private String guildName;

}
