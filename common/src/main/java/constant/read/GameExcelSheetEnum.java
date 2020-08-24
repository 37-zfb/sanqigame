package constant.read;


import constant.info.*;

/**
 * @author 张丰博
 */
public enum GameExcelSheetEnum {
    /**
     * 背包
     */
    BackPackConst("BackPackConst", BackPackConst.class),
    /**
     * boss
     */
    BossMonsterConst("BossMonsterConst", BossMonsterConst.class),
    /**
     * 副本
     */
    DuplicateConst("DuplicateConst", DuplicateConst.class),
    /**
     * 装备
     */
    EquipmentConst("EquipmentConst", EquipmentConst.class),
    /**
     *  邮件
     */
    MailConst("MailConst", MailConst.class),
    /**
     *  药剂
     */
    PotionConst("PotionConst",PotionConst.class),
    /**
     * 职业
     */
    ProfessionConst("ProfessionConst", ProfessionConst.class),

    /**
     *  队伍
     */
    GoodsEntity("TeamConst", TeamConst.class)
    ;




    GameExcelSheetEnum(String fileName, Class<?> clazz) {
        this.fileName = fileName;
        this.clazz = clazz;
    }

    /**
     *  文件名称
     */
    private String fileName;
    /**
     *  文件对应的 类
     */
    private Class<?> clazz;
    /**
     * 未使用字段
     */
    private String[] field;

    public static GameExcelSheetEnum getEnumByClass(Class<?> clazz) {
        for (GameExcelSheetEnum e : GameExcelSheetEnum.values()) {
            if (clazz.equals(e.getClazz())) {
                return e;
            }
        }
        return null;
    }

    public Class<?> getClazz() {
        return this.clazz;
    }

    public static String getNameByClazz(Class<?> clazz) {
        for (GameExcelSheetEnum e : GameExcelSheetEnum.values()) {
            if (clazz.equals(e.getClazz())) {
                return e.getName();
            }
        }
        return "";
    }

    public static Class<?> getClazzByName(String name){
        for (GameExcelSheetEnum e : GameExcelSheetEnum.values()) {
            if (name.equals(e.getName())) {
                return e.clazz;
            }
        }
        return null;
    }

    public String getName() {
        return this.fileName;
    }


}
