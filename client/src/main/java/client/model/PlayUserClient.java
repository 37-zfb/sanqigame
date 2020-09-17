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
    private volatile Integer currMp;

    /**
     * 当前蓝量
     */
    private volatile Integer currHp ;

    public PlayUserClient(Integer userId, String userName){
        this.userId = userId;
        this.userName = userName;
    }
    public PlayUserClient(Integer userId, String userName,Integer currMp,Integer currHp){
        this.userId = userId;
        this.userName = userName;
        this.currMp = currMp;
        this.currHp = currHp;
    }

}
