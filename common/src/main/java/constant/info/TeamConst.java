package constant.info;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author 张丰博
 */
@Getter
@ToString
public final class TeamConst {
    /**
     *  队伍最大数量
     */
    public  int MAX_NUMBER;
    private static TeamConst teamConst = null;

    private TeamConst() {
    }

    public static TeamConst getTeamConst(){
        return teamConst;
    }

    private void init(TeamConst teamConst){
        TeamConst.teamConst = teamConst;
    }
}
