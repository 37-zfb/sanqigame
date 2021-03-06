package server.scene;


import entity.conf.duplicate.BossEntity;
import entity.conf.duplicate.BossSkillEntity;
import entity.conf.duplicate.DuplicateEntity;
import entity.conf.guild.GuildRoleAuthEntity;
import entity.conf.guild.GuildRoleEntity;
import entity.conf.profession.*;
import entity.conf.props.EquipmentEntity;
import entity.conf.props.PotionEntity;
import entity.conf.props.PropsEntity;
import entity.conf.scene.MonsterEntity;
import entity.conf.scene.NpcEntity;
import entity.conf.scene.SceneEntity;
import entity.conf.store.GoodsEntity;
import entity.conf.task.TaskEntity;
import lombok.Setter;

;
import server.model.duplicate.BossMonster;
import server.model.duplicate.BossSkill;
import server.model.duplicate.Duplicate;
import server.model.guild.GuildRole;
import server.model.guild.GuildRoleAuth;
import server.model.profession.Profession;
import server.model.profession.Skill;
import server.model.profession.skill.PastorSkillProperty;
import server.model.profession.skill.SorceressSkillProperty;
import server.model.profession.skill.SummonerSkillProperty;
import server.model.profession.skill.WarriorSkillProperty;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import server.model.scene.Monster;
import server.model.scene.Npc;
import server.model.scene.Scene;
import server.model.store.Goods;
import server.model.task.Task;
import type.EquipmentType;
import type.ProfessionType;
import type.TaskType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author 张丰博
 */
@Setter
public class GameData {

    /**
     * 场景id ==> 场景对象
     */
    private final Map<Integer, Scene> sceneMap = new HashMap<>();

    /**
     * id  职业
     */
    private final Map<Integer, Profession> professionMap = new HashMap<>();

    /**
     * id  道具
     */
    private final Map<Integer, Props> propsMap = new HashMap<>();

    /**
     * id  副本
     */
    private final Map<Integer, Duplicate> duplicateMap = new HashMap<>();

    /**
     * id  商品
     */
    private final Map<Integer, Goods> goodsMap = new HashMap<>();

    /**
     * id  公会角色
     */
    private final Map<Integer, GuildRole> guildRoleMap = new HashMap<>();

    /**
     * id 任务
     */
    private final Map<Integer, Task> taskMap = new HashMap<>();

    private static final GameData GAME_DATA = new GameData();

    private GameData() {
    }

    public static GameData getInstance() {
        return GAME_DATA;
    }

    private List<PropsEntity> propsEntityList;

    private List<EquipmentEntity> equipmentEntityList;

    private List<PotionEntity> potionEntityList;

    private List<ProfessionEntity> professionEntityList;

    private List<SkillBaseInfoEntity> skillBaseInfoEntityList;

    private List<WarriorSkillPropertyEntity> warriorSkillPropertyEntityList;

    private List<PastorSkillPropertyEntity> pastorSkillPropertyEntityList;

    private List<SorceressSkillPropertyEntity> sorceressSkillPropertyEntityList;

    private List<SummonerSkillPropertyEntity> summonerSkillPropertyEntityList;

    private List<SceneEntity> sceneEntityList;

    private List<NpcEntity> npcEntityList;

    private List<MonsterEntity> monsterEntityList;


    private List<DuplicateEntity> duplicateEntityList;
    private List<BossEntity> bossEntityList;
    private List<BossSkillEntity> bossSkillEntityList;

    private List<GuildRoleEntity> guildRoleEntityList;
    private List<GuildRoleAuthEntity> guildRoleAuthEntityList;
    /**
     * 商品
     */
    private List<GoodsEntity> goodsEntityList;


    /**
     * 任务
     */
    private List<TaskEntity> taskEntityList;

    /**
     * 初始化游戏数据
     */
    public void initGameData() {
        initScene();
        initProps();
        initProfession();
        initDuplicate();
        initStore();
        initGuild();
        initTask();
    }

    private void initTask() {
        for (TaskEntity taskEntity : taskEntityList) {
            Task task = new Task();
            task.setId(taskEntity.getId());
            task.setTaskName(taskEntity.getTaskName());
            task.setDescription(taskEntity.getDescription());
            task.setExperience(taskEntity.getExperience());
            task.setRewardProps(taskEntity.getRewardProps());
            task.setRewardMoney(taskEntity.getRewardMoney());
            task.setTypeCode(taskEntity.getTypeCode());
            task.setSceneId(taskEntity.getSceneId());
            task.setTypeCode(taskEntity.getTypeCode());
            task.setDuplicateId(taskEntity.getDuplicateId());
            task.setNpcId(taskEntity.getNpcId());

            if (taskEntity.getDialogue() != null && !taskEntity.getDialogue().equals("")){
                task.setDialogue(taskEntity.getDialogue());
            }
            task.setNumber(taskEntity.getNumber());

            taskMap.put(task.getId(), task);
        }
        taskEntityList = null;
    }

    private void initGuild() {
        for (GuildRoleEntity roleEntity : guildRoleEntityList) {
            GuildRole guildRole = new GuildRole(roleEntity.getId(), roleEntity.getRole());
            guildRoleMap.put(roleEntity.getId(), guildRole);

            for (GuildRoleAuthEntity authEntity : guildRoleAuthEntityList) {
                if (authEntity.getRoleId().equals(guildRole.getId())) {
                    guildRole.setGuildRoleAuth(new GuildRoleAuth(authEntity.getId(), authEntity.getRoleId(), authEntity.getAuth()));
                    break;
                }
            }

        }

        guildRoleEntityList = null;
        guildRoleAuthEntityList = null;
    }

    private void initStore() {
        for (GoodsEntity goodsEntity : goodsEntityList) {
            goodsMap.putIfAbsent(goodsEntity.getId(), new Goods(goodsEntity.getId(), goodsEntity.getPropsId(), goodsEntity.getNumberLimit(), goodsEntity.getInfo(), goodsEntity.getPrice()));
        }

        goodsEntityList = null;
    }

    private void initDuplicate() {
        for (DuplicateEntity duplicateEntity : duplicateEntityList) {
            duplicateMap.put(duplicateEntity.getId(), new Duplicate(duplicateEntity.getId(), duplicateEntity.getName()));
        }
        for (BossEntity bossEntity : bossEntityList) {
            Map<Integer, BossMonster> bossMonsterMap = duplicateMap.get(bossEntity.getDuplicateId()).getBossMonsterMap();
            BossMonster bossMonster = new BossMonster(bossEntity.getId(), bossEntity.getDuplicateId(), bossEntity.getBossName(), bossEntity.getHp(), bossEntity.getBaseDamage());
            bossMonsterMap.put(bossEntity.getId(), bossMonster);
            for (BossSkillEntity bossSkillEntity : bossSkillEntityList) {
                if (bossSkillEntity.getBossId().equals(bossMonster.getId())) {
                    bossMonster.getBossSkillMap()
                            .put(bossSkillEntity.getId(),
                                    new BossSkill(bossSkillEntity.getId(), bossSkillEntity.getBossId(), bossSkillEntity.getName(), bossSkillEntity.getSkillDamage(), bossSkillEntity.getInfo()));
                }
            }

        }
        duplicateEntityList = null;
        bossSkillEntityList = null;


    }


    private void initProfession() {
        // 封装角色，技能
        for (ProfessionEntity professionEntity : professionEntityList) {
            ProfessionType professionType = ProfessionType.valueOf(professionEntity.getProfession());
            professionMap.put(professionEntity.getId(),
                    new Profession(professionEntity.getId(), professionType, professionEntity.getBaseDamage(), professionEntity.getBaseDefense()));

        }
        for (SkillBaseInfoEntity skillBaseInfoEntity : skillBaseInfoEntityList) {
            professionMap.get(skillBaseInfoEntity.getProfessionId()).getSkillMap()
                    .put(skillBaseInfoEntity.getId(), new Skill(skillBaseInfoEntity.getId(), skillBaseInfoEntity.getProfessionId(),
                            skillBaseInfoEntity.getName(), skillBaseInfoEntity.getCdTime(), skillBaseInfoEntity.getIntroduce(), skillBaseInfoEntity.getConsumeMp()));
        }
        //战士
        Map<Integer, Skill> warriorSkillMap = professionMap.get(ProfessionType.Warrior.getId()).getSkillMap();
        for (WarriorSkillPropertyEntity warriorSkillPropertyEntity : warriorSkillPropertyEntityList) {
            warriorSkillMap.get(warriorSkillPropertyEntity.getSkillId())
                    .setSkillProperty(
                            new WarriorSkillProperty(
                                    warriorSkillPropertyEntity.getId(),
                                    warriorSkillPropertyEntity.getSkillId(),
                                    warriorSkillPropertyEntity.getEffectTime(),
                                    warriorSkillPropertyEntity.getDamage(),
                                    warriorSkillPropertyEntity.getPercentDamage()));

        }

        //牧师
        Map<Integer, Skill> pastorSkillMap = professionMap.get(ProfessionType.Pastor.getId()).getSkillMap();
        for (PastorSkillPropertyEntity pastorSkillPropertyEntity : pastorSkillPropertyEntityList) {
            pastorSkillMap.get(pastorSkillPropertyEntity.getSkillId())
                    .setSkillProperty(
                            new PastorSkillProperty(pastorSkillPropertyEntity.getId(),
                                    pastorSkillPropertyEntity.getSkillId(),
                                    pastorSkillPropertyEntity.getRecoverMp(),
                                    pastorSkillPropertyEntity.getRecoverHp(),
                                    pastorSkillPropertyEntity.getPercentMp(),
                                    pastorSkillPropertyEntity.getPercentHp(),
                                    pastorSkillPropertyEntity.getPrepareTime(),
                                    pastorSkillPropertyEntity.getShieldValue()));
        }

        //法师
        Map<Integer, Skill> sorceressSkillMap = professionMap.get(ProfessionType.Sorceress.getId()).getSkillMap();
        for (SorceressSkillPropertyEntity sorceressSkillPropertyEntity : sorceressSkillPropertyEntityList) {
            sorceressSkillMap.get(sorceressSkillPropertyEntity.getSkillId())
                    .setSkillProperty(
                            new SorceressSkillProperty(sorceressSkillPropertyEntity.getId(),
                                    sorceressSkillPropertyEntity.getSkillId(),
                                    sorceressSkillPropertyEntity.getDamageValue(),
                                    sorceressSkillPropertyEntity.getDamagePercent()
                            ));
        }

        //法师
        Map<Integer, Skill> summonerSkillMap = professionMap.get(ProfessionType.Summoner.getId()).getSkillMap();
        for (SummonerSkillPropertyEntity summonerSkillPropertyEntity : summonerSkillPropertyEntityList) {
            summonerSkillMap.get(summonerSkillPropertyEntity.getSkillId())
                    .setSkillProperty(
                            new SummonerSkillProperty(summonerSkillPropertyEntity.getId(),
                                    summonerSkillPropertyEntity.getSkillId(),
                                    summonerSkillPropertyEntity.getEffectTime(),
                                    summonerSkillPropertyEntity.getPetDamage()
                            ));
        }
        professionEntityList = null;
        skillBaseInfoEntityList = null;
        warriorSkillPropertyEntityList = null;
        pastorSkillPropertyEntityList = null;
        sorceressSkillPropertyEntityList = null;
        summonerSkillPropertyEntityList = null;
    }

    private void initProps() {
        // 封装道具
        for (PropsEntity propsEntity : propsEntityList) {
            propsMap.put(propsEntity.getId(), new Props(propsEntity.getId(), propsEntity.getName()));
        }
        for (EquipmentEntity equipmentEntity : equipmentEntityList) {
            for (Props value : propsMap.values()) {
                if (equipmentEntity.getPropsId().equals(value.getId())) {
                    EquipmentType equipmentType = EquipmentType.valueOf(equipmentEntity.getType());
                    value.setPropsProperty(
                            new Equipment((long) equipmentEntity.getId(), equipmentEntity.getPropsId(),
                                    equipmentEntity.getDurability(), equipmentEntity.getDamage(), equipmentType));
                    break;
                }
            }
        }
        for (PotionEntity potionEntity : potionEntityList) {
            for (Props value : propsMap.values()) {
                if (potionEntity.getPropsId().equals(value.getId())) {
                    value.setPropsProperty(new Potion((long) potionEntity.getId(), potionEntity.getPropsId(), potionEntity.getCdTime(),
                            potionEntity.getInfo(), potionEntity.getResumeFigure(), potionEntity.getPercent()));
                }
            }
        }

        propsEntityList = null;
        equipmentEntityList = null;
    }


    private void initScene() {
        // 封装场景
        for (SceneEntity sceneEntity : sceneEntityList) {
            sceneMap.put(sceneEntity.getId(), new Scene(sceneEntity.getId(), sceneEntity.getName()));
        }
        for (NpcEntity npcEntity : npcEntityList) {
            sceneMap.get(npcEntity.getSceneId()).getNpcMap().put(npcEntity.getId(),
                    new Npc(npcEntity.getId(), npcEntity.getName(), npcEntity.getSceneId(), npcEntity.getInfo()));
        }
        for (MonsterEntity monsterEntity : monsterEntityList) {
            sceneMap.get(monsterEntity.getSceneId()).getMonsterMap().put(monsterEntity.getId(),
                    new Monster(monsterEntity.getId(), monsterEntity.getSceneId(), monsterEntity.getName(),
                            monsterEntity.getHp(), monsterEntity.getPropsId()));
        }
        sceneEntityList = null;
        npcEntityList = null;
        monsterEntityList = null;
    }


    public Map<Integer, Scene> getSceneMap() {
        return sceneMap;
    }

    public Map<Integer, Profession> getProfessionMap() {
        return professionMap;
    }

    public Map<Integer, Props> getPropsMap() {
        return propsMap;
    }

    public Map<Integer, Duplicate> getDuplicateMap() {
        return duplicateMap;
    }

    public Map<Integer, Goods> getGoodsMap() {
        return goodsMap;
    }

    public Map<Integer, Task> getTaskMap() {
        return taskMap;
    }
}
