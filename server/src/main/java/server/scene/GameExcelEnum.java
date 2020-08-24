package server.scene;


/**
 * @author 张丰博
 */

public enum GameExcelEnum {
    /**
     *  道具
     */
    Props("props.xls"),
    /**
     *  角色
     */
    Role("role.xls"),
    /**
     *  场景
     */
    Scene("scene.xls"),
    /**
     *  副本
     */
    Duplicate("duplicate.xls"),
    /**
     *  商店
     */
    Store("store.xls")

    ;

    /**
     * 文件名称
     */
    private String fileName;


    GameExcelEnum(String fileName) {
        this.fileName = fileName;
    }


    public String getName() {
        return this.fileName;
    }
}
