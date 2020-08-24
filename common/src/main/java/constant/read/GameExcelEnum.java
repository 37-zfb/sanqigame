package constant.read;


/**
 * @author 张丰博
 */

public enum GameExcelEnum {
    /**
     *  常量
     */
    CONST("const.xls"),
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
