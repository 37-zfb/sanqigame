package server.scene;


import entity.conf.profession.WarriorSkillPropertyEntity;

/**
 * @author 张丰博
 */
public enum GameExcelSheetEnum {
    /**
     * npc
     */
    NpcEntity("NpcEntity", entity.conf.scene.NpcEntity.class),
    /**
     * 场景
     */
    SceneEntity("SceneEntity", entity.conf.scene.SceneEntity.class),
    /**
     * 怪
     */
    MonsterEntity("MonsterEntity", entity.conf.scene.MonsterEntity.class),
    /**
     * 技能基本信息
     */
    SkillBaseInfoEntity("SkillBaseInfoEntity", entity.conf.profession.SkillBaseInfoEntity.class),
    /**
     *  职业
     */
    ProfessionEntity("ProfessionEntity", entity.conf.profession.ProfessionEntity.class),
    /**
     *  战士
     */
    WarriorSkillPropertyEntity("WarriorSkillPropertyEntity",WarriorSkillPropertyEntity.class),
    /**
     * 牧师
     */
    PastorSkillPropertyEntity("PastorSkillPropertyEntity", entity.conf.profession.PastorSkillPropertyEntity.class),
    /**
     *  法师
     */
    SorceressSkillPropertyEntity("SorceressSkillPropertyEntity", entity.conf.profession.SorceressSkillPropertyEntity.class),
    /**
     * 召唤师
     */
    SummonerSkillPropertyEntity("SummonerSkillPropertyEntity", entity.conf.profession.SummonerSkillPropertyEntity.class),
    /**
     *  道具
     */
    PropsEntity("PropsEntity", entity.conf.props.PropsEntity.class),
    /**
     *  装备
     */
    EquipmentEntity("EquipmentEntity", entity.conf.props.EquipmentEntity.class),
    /**
     * 药剂
     */
    PotionEntity("PotionEntity", entity.conf.props.PotionEntity.class),

    /**
     *  副本
     */
    DuplicateEntity("DuplicateEntity", entity.conf.duplicate.DuplicateEntity.class),

    /**
     *  boss
     */
    BossEntity("BossEntity", entity.conf.duplicate.BossEntity.class),

    /**
     *  boss技能
     */
    BossSkillEntity("BossSkillEntity", entity.conf.duplicate.BossSkillEntity.class),
    /**
     *  商店
     */
    GoodsEntity("GoodsEntity", entity.conf.store.GoodsEntity.class)
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
