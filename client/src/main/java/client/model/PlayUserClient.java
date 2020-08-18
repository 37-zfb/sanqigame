package client.model;

import constant.ProfessionConst;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
public class PlayUserClient {

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
    private volatile Integer currMp = ProfessionConst.HP;

    /**
     * 当前蓝量
     */
    private volatile Integer currHp = ProfessionConst.MP;

    public PlayUserClient(Integer userId, String userName){
        this.userId = userId;
        this.userName = userName;
    }

}
