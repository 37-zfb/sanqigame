package client.model.team;

import client.model.PlayUserClient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

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
    private Set<Integer> originateIdSet = new HashSet<>();

    /**
     *  队长id
     */
    private Integer teamLeaderId;

    /**
     *  队伍成员
     */
    private PlayUserClient[] teamMember = new PlayUserClient[4];

}
