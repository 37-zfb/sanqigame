package client.model.team;

import client.model.PlayUserClient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author 张丰博
 */
@Setter
@Getter
@NoArgsConstructor
public class PlayTeamClient {

    /**
     *  发起者id
     */
    private Integer originateUserId;

    /**
     *  队长id
     */
    private Integer teamLeaderId;

    /**
     *  队伍成员
     */
    private PlayUserClient[] teamMember = new PlayUserClient[4];

}
