package server.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import server.model.duplicate.Duplicate;

import java.util.HashSet;
import java.util.Set;

/**
 * @author 张丰博
 */
@Slf4j
@Setter
@Getter
@NoArgsConstructor
public class PlayTeam {

    /**
     * 当前所在副本
     */
    private Duplicate currDuplicate;

    /**
     *  加入队伍监视器
     */
    private final Object TEAM_MONITOR = new Object();

    /**
     * 队长id
     */
    private Integer teamLeaderId;

    /**
     * 队伍成员id
     */
    private final  Integer[] TEAM_MEMBER = new Integer[4];

    /**
     *  队伍成员数
     */
    private int teamNumber = 0;


}
