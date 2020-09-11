package exception;

/**
 * @author 张丰博
 */

public enum CustomizeErrorCode implements ICustomizeErrorCode {
    /**
     *  用户名或密码不正确
     */
    USER_NOT_FOUND(-413,"用户名或密码不正确;"),
    /**
     *  用户名已经存在
     */
    USER_EXISTS(-414,"用户名已经存在;"),
    /**
     *  用户不存在
     */
    USER_NOT_EXISTS(-415,"用户不存在"),
    /**
     *  耐久度不足
     */
    DURABILITY(-450,"耐久度不足;"),
    /**
     *  背包空间不足
     */
    BACKPACK_SPACE_INSUFFICIENT(-550,"背包空间不足;"),
    /**
     *  此道具已达上限
     */
    PROPS_REACH_LIMIT(-551,"此道具已达上限;"),
    /**
     *  用户金币不足
     */
    USER_MONEY_INSUFFICIENT(-552,"用户金币不足;"),
    /**
     *  商品剩余数量不足
     */
    ALLOW_BUY_NUMBER_INSUFFICIENT(-553,"商品剩余数量不足;"),
    /**
     *  用户没有被管理
     */
    USER_NOT_MANAGER(-416,"用户没有被管理;"),

    /**
     * 交易
     */
    DEAL_REQUEST_ERROR(-417,"不能和自己交易;"),
    USER_NOT_DEAL_STATUS(-418,"不是交易状态;"),
    ORIGINATE_USER_NOT_FOUNT(-418,"发起者不存在"),
    ORIGINATE_USER_NOT_REQUEST(-419,"发起者没有发起;"),

    /**
     * 公会
     */
    USER_MONEY_NOT_ENOUGH(-420,"金币不足;"),
    USER_HAVE_GUILD(-421,"已有公会;"),
    GUILD_ALREADY_EXIST(-422,"公会名已存在;"),
    USER_NO_HAVE_GUILD_OR_NOT_PRESIDENT(-423,"用户没有公会或不是会长;"),
    GUILD_NOT_EXIST(-424,"公会不存在;"),
    GUILD_REACH_LIMIT(-425,"公会人数已到达上限;"),
    NOT_JOIN_GUILD(-426,"未加入公会;"),
    NO_HAVE_MEMBER(-427,"没有该成员;"),
    WAREHOUSE_NO_PROPS(-428,"没有此道具;"),
    WAREHOUSE_NO_MONEY(-429,"没有怎么多钱;"),
    USER_NOT_PROPS(-430,"用户没有此道具;"),
    WAREHOUSE_SPACE_INSUFFICIENT(-431,"仓库空间不足;"),
    POTION_INSUFFICIENT(-432,"药剂数量不够;"),
    PROPS_NOT_EXIST(-433,"道具不存在;"),
    AUTH_NOT_ENOUGH(-434,"权限不够;"),
    WAREHOUSE_POTION_NUMBER_NOT_ENOUGH(-435,"药剂数量不足;"),
    NOT_QUIT(-438,"会长不能退出;"),
    NOT_JOIN_THIS_GUILD(-439,"未加入此公会;"),

    /**
     * 拍卖行
     */
    ADD_ITEM_ERROR(-436,"添加拍卖品失败;"),
    ITEM_NOT_FOUNT(-437,"拍卖品不存在;"),
    ALREADY_JOIN_BIDDING(-440,"已经参与竞拍;"),
    FRIEND_REACH_LIMIT(-441,"好友已达上限;"),

    USER_ALREADY_LOGIN(-442,"用户已经登陆;"),
    USER_NOT_HAVE_THIS_SKILL(-443,"该用户不存在此技能;"),
    SCENE_NOT_MONSTER(-444,"当前场景没有怪;"),
    SKILL_CD(-445,"技能冷却中;"),
    MP_NOT_ENOUGH(-446,"MP不足;"),
    POTION_CD_TIME(-447,"该药剂在cd中;"),
    TARGET_USER_QUIT(-448,"对方已离线;"),
    USER_NOT_ARENA(-449,"不在竞技场中;"),
    NOT_TEAM_LEADER(-460,"不是队长;"),
    DUPLICATE_TIME_OUT(-461,"副本超时;"),
    ALL_MONSTER_DIE(-462,"当前场景没有存活的怪;"),
    GOODS_NOT_EXIST(-463,"不存在该商品;"),
    MAIL_NUMBER_OVERFLOW(-464,"邮件道具个数超出最大数量;"),
    USER_ALREADY_IN_ARENA(-465,"用户已存在竞技场;"),
    TARGET_NOT_EXIST(-466,"目标用户不在竞技场;"),
    USER_NOT_INVITE(-466,"未被邀请;"),
    ;

    private String message;

    private Integer code;

    CustomizeErrorCode( Integer code,String message) {
        this.message = message;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }
}
