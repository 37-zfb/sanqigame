package server.cmdhandler;

import constant.EquipmentConst;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import model.props.AbstractPropsProperty;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import entity.db.CurrUserStateEntity;
import entity.db.UserEntity;
import entity.db.UserEquipmentEntity;
import entity.db.UserPotionEntity;
import server.exception.CustomizeErrorCode;
import server.exception.CustomizeException;
import server.model.*;
import model.profession.Skill;
import model.props.Equipment;
import model.props.Potion;
import model.props.Props;
import model.scene.Monster;
import model.scene.Npc;
import model.scene.Scene;
import scene.GameData;
import server.service.UserService;
import server.Broadcast;
import type.PropsType;

import java.util.List;
import java.util.Map;

/**
 * 用户登录
 *
 * @author 张丰博
 */
@Slf4j
@Component
public class UserLoginCmdHandler implements ICmdHandler<GameMsg.UserLoginCmd> {

    @Autowired
    private UserService userService;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserLoginCmd cmd) {

        if (ctx == null || cmd == null) {
            return;
        }

        String userName = cmd.getUserName();
        String password = cmd.getPassword();

        log.info("用户登陆:{}", userName);
        log.info("当前线程:{}", Thread.currentThread().getName());

        GameMsg.UserLoginResult.Builder resultBuilder = GameMsg.UserLoginResult.newBuilder();
        GameMsg.UserLoginResult loginResult = null;
        try {
            UserEntity userEntity = userService.getUserByName(userName);
            if (!password.equals(userEntity.getPassword())) {
                throw new CustomizeException(CustomizeErrorCode.USER_NOT_FOUND);
            }

            log.info("登陆成功:userId={},userName={}", userEntity.getId(), userEntity.getUserName());
            log.info("当前线程:{}", Thread.currentThread().getName());

            CurrUserStateEntity userState = userService.getCurrUserStateByUserId(userEntity.getId());

            User user = createUser(userEntity, userState);
            user.setCtx(ctx);

            // 添加管道
            Broadcast.addChannel(user.getCurSceneId(), ctx.channel());
            // 将用户id附着到channel中
            ctx.channel().attr(AttributeKey.valueOf("userId")).set(userEntity.getId());
            UserManager.addUser(user);

            // 用户基本信息
            resultBuilder.setUserId(user.getUserId())
                    .setUserName(user.getUserName())
                    .setHp(user.getCurrHp())
                    .setMp(user.getCurrMp())
                    .setCurrSceneId(user.getCurSceneId())
                    .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                    .setProfessionId(user.getProfessionId());

            GameData gameData = GameData.getInstance();
            //封装 当前用户的技能.
            for (Skill skill : user.getSkillMap().values()) {
                GameMsg.UserLoginResult.Skill.Builder skillBuilder = GameMsg.UserLoginResult.Skill.newBuilder()
                        .setId(skill.getId());
                resultBuilder.addSkill(skillBuilder);
            }

            Scene scene = gameData.getSceneMap().get(user.getCurSceneId());
            // 封装当前场景npc
            for (Npc npc : scene.getNpcMap().values()) {
                GameMsg.UserLoginResult.Npc.Builder npcBuilder = GameMsg.UserLoginResult.Npc.newBuilder()
                        .setName(npc.getName())
                        .setId(npc.getId())
                        .setInfo(npc.getInfo());
                resultBuilder.addNpc(npcBuilder);
            }
            // 封装当前场景 怪
            for (Monster monster : scene.getMonsterMap().values()) {
                GameMsg.UserLoginResult.Monster monsterBuilder = GameMsg.UserLoginResult.Monster.newBuilder()
                        .setId(monster.getId())
                        .setName(monster.getName())
                        .setHp(monster.getHp())
                        .setIsDie(monster.isDie())
                        .build();
                resultBuilder.addMonster(monsterBuilder);
            }

            //  背包中的道具(装备、药剂等)
            Map<Integer, Props> backpack = user.getBackpack();
            for (Map.Entry<Integer, Props> propsEntry : backpack.entrySet()) {
                GameMsg.UserLoginResult.Props.Builder propsResult = GameMsg.UserLoginResult.Props.newBuilder()
                        .setLocation(propsEntry.getKey())
                        .setPropsId(propsEntry.getValue().getId());

                AbstractPropsProperty propsProperty = propsEntry.getValue().getPropsProperty();
                if (propsProperty.getType() == PropsType.Equipment){
                    Equipment equipment = (Equipment) propsProperty;
                    // equipment.getId() 是数据库中的user_equipment中的id
                    propsResult.setDurability(equipment.getDurability()).setUserPropsId(equipment.getId());
                }else if (propsProperty.getType() == PropsType.Potion){
                    Potion potion = (Potion) propsProperty;
                    //potion.getId() 是数据库中的 user_potion中的id
                    propsResult.setPropsNumber(potion.getNumber()).setUserPropsId(potion.getId());
                }
                resultBuilder.addProps(propsResult);
            }



            UserEquipmentEntity[] userEquipmentArr = user.getUserEquipmentArr();
            for (UserEquipmentEntity userEquipmentEntity : userEquipmentArr) {
                if (userEquipmentEntity != null) {
                    GameMsg.UserLoginResult.WearEquipment.Builder builder = GameMsg.UserLoginResult.WearEquipment.newBuilder()
                            //userEquipmentEntity.getId() 数据库中 user_equipment中的id
                            .setId(userEquipmentEntity.getId())
                            .setEquipmentId(userEquipmentEntity.getPropsId())
                            .setDurability(userEquipmentEntity.getDurability());
                    resultBuilder.addWearEqu(builder);
                }
            }

            loginResult = resultBuilder.build();
        } catch (CustomizeException e) {
            loginResult = resultBuilder.setUserId(e.getCode()).build();
            log.error(e.getMessage(), e);
        } finally {
            ctx.channel().writeAndFlush(loginResult);
        }


    }

    /**
     * 创建user模型
     *
     * @param userEntity 用户实体
     * @param userState  用户状态
     * @return user对象
     */
    private User createUser(UserEntity userEntity, CurrUserStateEntity userState) {
        User user = new User();
        user.setProfessionId(userEntity.getProfessionId());
        user.setUserId(userEntity.getId());
        user.setCurSceneId(userState.getCurrSceneId());
        user.setCurrHp(userState.getCurrHp());
        user.setCurrMp(userState.getCurrMp());
        user.setUserName(userEntity.getUserName());
        user.setBaseDamage(userState.getBaseDamage());
        user.setBaseDefense(userState.getBaseDefense());


        // 封装技能
        Map<Integer, Skill> skillMap = GameData.getInstance().getProfessionMap().get(user.getProfessionId()).getSkillMap();
        Map<Integer, Skill> currSkillMap = user.getSkillMap();
        for (Skill skill : skillMap.values()) {
            currSkillMap.put(skill.getId(),
                    new Skill(skill.getId(), skill.getProfessionId(), skill.getName(), skill.getCdTime(), skill.getIntroduce(), skill.getConsumeMp(),skill.getSkillProperty()));
        }


        loadBackpack(user);
        loadWearEqu(user);
        // 启动定时器
//        user.startTimer();
        // 设置mp恢复结束时间
        user.resumeMpTime();


        return user;
    }

    /**
     *  加载背包中的道具
     * @param user
     */
    private void loadBackpack(User user) {
        // 装备
        List<UserEquipmentEntity> listEquipment = userService.listEquipment(user.getUserId());
        // 药剂
        List<UserPotionEntity> listPotion = userService.listPotion(user.getUserId());

        // 背包，  位置 道具
        Map<Integer, Props> backpack = user.getBackpack();

        // 所有的道具
        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        for (UserEquipmentEntity equipmentEntity : listEquipment) {
            // 装备id，就是道具id
            Props props = propsMap.get(equipmentEntity.getPropsId());
            Equipment equipment = (Equipment) props.getPropsProperty();
            //equipmentEntity.getId() 是数据库中的user_equipment中的id
            backpack.put(equipmentEntity.getLocation(),new Props(props.getId(),props.getName(),new Equipment(equipmentEntity.getId(),
                    props.getId(),equipmentEntity.getDurability(),equipment.getDamage(),equipment.getEquipmentType())));
        }

        for (UserPotionEntity potionEntity : listPotion) {
            // 药剂id，就是道具id
            Props props = propsMap.get(potionEntity.getPropsId());
            Potion potion = (Potion) props.getPropsProperty();
            //potionEntity.getId() 是数据库 user_potion中的id
            backpack.put(potionEntity.getLocation(),new Props(props.getId(),props.getName(),new Potion(potionEntity.getId(),potion.getPropsId(),
                    potion.getCdTime(),potion.getInfo(),potion.getResumeFigure(),potion.getPercent(),potionEntity.getNumber())));
        }
    }

    private void loadWearEqu(User user) {
        UserEquipmentEntity[] userEquipmentArr = user.getUserEquipmentArr();
        List<UserEquipmentEntity> listEquipment = userService.listEquipmentWeared(user.getUserId(), EquipmentConst.WEAR);
        for (int i = 0; i < listEquipment.size(); i++) {
            userEquipmentArr[i] = listEquipment.get(i);
        }


    }

}