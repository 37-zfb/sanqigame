package client.model.arena;

import constant.ProfessionConst;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
public class ArenaUser {

    /**
     *  用户id
     */
    private Integer userId;

    /**
     *  用户名字
     */
    private String userName;

    /**
     *  当前血量
     */
    private Integer currMp = ProfessionConst.HP;

    /**
     * 当前蓝量
     */
    private volatile Integer currHp = ProfessionConst.MP;

    public ArenaUser(Integer userId,String userName){
        this.userId = userId;
        this.userName = userName;
    }

}
