package type;

/**
 * @author 张丰博
 */
public enum MailType {
    /**
     *  邮件未读
     */
    UNREAD(0,"未读"),
    /**
     *  邮件已读
     */
    READ(1,"已读"),
    /**
     *  邮件过期
     */
    EXPIRED(3,"过期"),
    /**
     *  一键领取
     */
    RECEIVE_ALL(0,"领取全部"),
    ;
    /**
     *  状态码
     */
    private Integer state;

    /**
     *  描述信息
     */
    private String info;

    MailType(Integer state,String info){
        this.state = state;
        this.info = info;
    }

    public Integer getState() {
        return state;
    }

    public String getInfo() {
        return info;
    }
}
