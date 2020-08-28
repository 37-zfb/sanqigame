package type;

/**
 * @author 张丰博
 */
public enum GuildMemberType {

    /**
     * 为加入公会
     */
    Public(0, "未加入公会;"),

    /**
     * 会长
     */
    President(1, "会长"),
    /**
     * 副会长
     */
    VicePresident(2, "副会长"),
    /**
     * 精英
     */

    Elite(3, "精英"),
    /**
     * 普通会员
     */
    Member(4, "普通会员"),
    ;

    private Integer roleId;


    private String roleName;

    GuildMemberType(Integer roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public static String getRoleNameByRoleId(Integer roleId) {
        if (roleId != null) {
            for (GuildMemberType value : GuildMemberType.values()) {
                if (value.getRoleId().equals(roleId)) {
                    return value.getRoleName();
                }
            }
        }
        return null;
    }

}
