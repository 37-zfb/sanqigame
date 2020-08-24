package client.model.server.duplicate;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
public class ForceAttackUser {

    private Integer userId;

    private long startTime;

    private long endTime;

}
