package server.cmdhandler;

import com.alibaba.fastjson.JSON;
import constant.EquipmentConst;
import constant.MailConst;
import entity.db.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import server.GuildManager;
import server.model.*;
import server.model.store.Goods;
import server.model.props.AbstractPropsProperty;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import server.model.profession.Skill;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import server.model.scene.Monster;
import server.model.scene.Npc;
import server.model.scene.Scene;
import server.scene.GameData;
import server.service.GuildService;
import server.service.MailService;
import server.service.UserService;
import server.Broadcast;
import type.GoodsLimitBuyType;
import type.GuildMemberType;
import type.MailType;
import type.PropsType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    @Autowired
    private MailService mailService;
    @Autowired
    private GuildService guildService;

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
            if (userEntity == null ||
                    !password.equals(userEntity.getPassword())) {
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
                    .setProfessionId(user.getProfessionId())
                    .setMoney(user.getMoney());

            //封装 当前用户的技能.
            packageSkill(user, resultBuilder);

            //Npc 、怪
            packageScene(user, resultBuilder);

            //  背包中的道具(装备、药剂等)
            packageBackpack(user, resultBuilder);

            // 商品限制
            packageStore(user, resultBuilder);

            //封装邮件
            packageMail(user, resultBuilder);

            //封装公会
            packageGuild(user, resultBuilder);


            loginResult = resultBuilder.build();
        } catch (CustomizeException e) {
            loginResult = resultBuilder.setUserId(e.getCode()).build();
            log.error(e.getMessage(), e);
        } catch (NullPointerException e) {
            log.error(e.getMessage(), e);
        } finally {
            ctx.channel().writeAndFlush(loginResult);
        }

    }

    /**
     * 封装公会
     *
     * @param user          用户对象
     * @param resultBuilder 结果构建者
     */
    private void packageGuild(User user, GameMsg.UserLoginResult.Builder resultBuilder) {
        if (user.getPlayGuild() == null) {
            return;
        }
        PlayGuild playGuild = user.getPlayGuild();
        resultBuilder.setGuildName(playGuild.getGuildEntity().getGuildName())
                .setGuildPosition(playGuild.getGuildMemberMap().get(user.getUserId()).getGuildPosition());
    }

    /**
     * 封装邮件
     *
     * @param user          用户对象
     * @param resultBuilder 结果构建者
     */
    private void packageSkill(User user, GameMsg.UserLoginResult.Builder resultBuilder) {
        for (Skill skill : user.getSkillMap().values()) {
            GameMsg.UserLoginResult.Skill.Builder skillBuilder = GameMsg.UserLoginResult.Skill.newBuilder()
                    .setId(skill.getId());
            resultBuilder.addSkill(skillBuilder);
        }
    }

    /**
     * 封装邮件
     *
     * @param user          用户对象
     * @param resultBuilder 结果构建者
     */
    private void packageMail(User user, GameMsg.UserLoginResult.Builder resultBuilder) {
        Map<Long, DbSendMailEntity> mailEntityMap = user.getMail().getMailEntityMap();
        for (DbSendMailEntity mailEntity : mailEntityMap.values()) {
            if (resultBuilder.getMailInfoCount() == MailConst.MAX_SHOW_NUMBER) {
                // 最多一次性发送两百封邮件
                break;
            }
            GameMsg.MailInfo mailInfo = GameMsg.MailInfo.newBuilder()
                    .setSrcUserName(mailEntity.getSrcUserName())
                    .setTitle(mailEntity.getTitle())
                    .setMailId(mailEntity.getId())
                    .build();
            resultBuilder.addMailInfo(mailInfo);
        }
    }

    /**
     * 封装商品限制
     *
     * @param user          用户对象
     * @param resultBuilder 结果构建者
     */
    private void packageStore(User user, GameMsg.UserLoginResult.Builder resultBuilder) {
        Map<Integer, Integer> goodsAllowNumber = user.getGoodsAllowNumber();
        for (Map.Entry<Integer, Integer> integerEntry : goodsAllowNumber.entrySet()) {
            GameMsg.UserLoginResult.GoodsLimit goodsLimit = GameMsg.UserLoginResult.GoodsLimit.newBuilder()
                    .setGoodsId(integerEntry.getKey())
                    .setGoodsNumber(integerEntry.getValue())
                    .build();
            resultBuilder.addGoodLimits(goodsLimit);
        }
    }

    /**
     * 封装背包和装备栏
     *
     * @param user          用户对象
     * @param resultBuilder 结果构建者
     */
    private void packageBackpack(User user, GameMsg.UserLoginResult.Builder resultBuilder) {
        Map<Integer, Props> backpack = user.getBackpack();
        for (Map.Entry<Integer, Props> propsEntry : backpack.entrySet()) {
            GameMsg.Props.Builder propsResult = GameMsg.Props.newBuilder()
                    .setLocation(propsEntry.getKey())
                    .setPropsId(propsEntry.getValue().getId());

            AbstractPropsProperty propsProperty = propsEntry.getValue().getPropsProperty();
            if (propsProperty.getType() == PropsType.Equipment) {
                Equipment equipment = (Equipment) propsProperty;
                // equipment.getId() 是数据库中的user_equipment中的id
                propsResult.setDurability(equipment.getDurability()).setUserPropsId(equipment.getId());
            } else if (propsProperty.getType() == PropsType.Potion) {
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
    }

    /**
     * 封装场景
     *
     * @param user          用户对象
     * @param resultBuilder 结果构建者
     */
    private void packageScene(User user, GameMsg.UserLoginResult.Builder resultBuilder) {
        Scene scene = GameData.getInstance().getSceneMap().get(user.getCurSceneId());
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
        user.setMoney(userState.getMoney());

        if (!userState.getGuildId().equals(GuildMemberType.Public.getRoleId())) {
            GuildMemberEntity memberEntity = guildService.findGuildMemberById(user.getUserId());
            if (memberEntity != null){
                PlayGuild playGuild = GuildManager.getGuild(userState.getGuildId());
                playGuild.getGuildMemberMap().get(user.getUserId()).setOnline(true);
                user.setPlayGuild(playGuild);
            }else {
                userState.setGuildId(GuildMemberType.Public.getRoleId());
                userService.modifyUserState(userState);
            }
        }

        // 封装技能
        Map<Integer, Skill> skillMap = GameData.getInstance().getProfessionMap().get(user.getProfessionId()).getSkillMap();
        Map<Integer, Skill> currSkillMap = user.getSkillMap();
        for (Skill skill : skillMap.values()) {
            currSkillMap.put(skill.getId(),
                    new Skill(skill.getId(), skill.getProfessionId(), skill.getName(), skill.getCdTime(), skill.getIntroduce(), skill.getConsumeMp(), skill.getSkillProperty()));
        }

        loadBackpack(user);
        loadWearEqu(user);
        loadLimitNumber(user);
        loadMail(user);
        // 群发邮件
        //sendMailAll(user);

        // 启动定时器
//        user.startTimer();
        // 设置mp恢复结束时间
        user.resumeMpTime();

        return user;
    }


    /**
     * 群发邮件
     *
     * @param user 用户对象
     */
    private void sendMailAll(User user) {
        DbSendMailEntity dbSendMailEntity = new DbSendMailEntity();
        dbSendMailEntity.setTargetUserId(user.getUserId());
        dbSendMailEntity.setSrcUserId(0);
        dbSendMailEntity.setMoney(10000);
        dbSendMailEntity.setState(MailType.UNREAD.getState());
        dbSendMailEntity.setDate(new Date());
        dbSendMailEntity.setTitle("周年奖励;");
        dbSendMailEntity.setSrcUserName("管理员");

        List<MailProps> mailPropsList = new ArrayList<>();
        mailPropsList.add(new MailProps());
        String jsonString = JSON.toJSONString(mailPropsList);
        dbSendMailEntity.setPropsInfo(jsonString);

        DbSendMailEntity mailEntity = mailService.findMailInfoByUserIdAndTitle(user.getUserId(), dbSendMailEntity.getTitle());
        if (mailEntity == null) {
            PlayMail mail = user.getMail();
            Map<Long, DbSendMailEntity> mailEntityMap = mail.getMailEntityMap();
            mailEntityMap.put(dbSendMailEntity.getId(), dbSendMailEntity);
            mailService.addMailInfo(dbSendMailEntity);
        }
    }

    /**
     * 加载未读的邮件
     *
     * @param user 用户对象
     */
    private void loadMail(User user) {
        List<DbSendMailEntity> mailEntityList = mailService.listMailWithinTenDay(user.getUserId());
        PlayMail mail = user.getMail();
        Map<Long, DbSendMailEntity> mailEntityMap = mail.getMailEntityMap();
        for (DbSendMailEntity dbSendMailEntity : mailEntityList) {
            mailEntityMap.put(dbSendMailEntity.getId(), dbSendMailEntity);
        }
    }

    /**
     * 加载背包中的道具
     *
     * @param user 用户对象
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
            backpack.put(equipmentEntity.getLocation(), new Props(props.getId(), props.getName(), new Equipment(equipmentEntity.getId(),
                    props.getId(), equipmentEntity.getDurability(), equipment.getDamage(), equipment.getEquipmentType())));
        }

        for (UserPotionEntity potionEntity : listPotion) {
            // 药剂id，就是道具id
            Props props = propsMap.get(potionEntity.getPropsId());
            Potion potion = (Potion) props.getPropsProperty();
            //potionEntity.getId() 是数据库 user_potion中的id
            backpack.put(potionEntity.getLocation(), new Props(props.getId(), props.getName(), new Potion(potionEntity.getId(), potion.getPropsId(),
                    potion.getCdTime(), potion.getInfo(), potion.getResumeFigure(), potion.getPercent(), potionEntity.getNumber())));
        }
    }

    /**
     * 加载穿戴的装备
     * @param user 用户对象
     */
    private void loadWearEqu(User user) {
        UserEquipmentEntity[] userEquipmentArr = user.getUserEquipmentArr();
        List<UserEquipmentEntity> listEquipment = userService.listEquipmentWeared(user.getUserId(), EquipmentConst.WEAR);
        for (int i = 0; i < listEquipment.size(); i++) {
            userEquipmentArr[i] = listEquipment.get(i);
        }
    }

    /**
     * 加载限制数量
     *
     * @param user 用户对象
     */
    private void loadLimitNumber(User user) {

        Map<Integer, Goods> goodsMap = GameData.getInstance().getGoodsMap();

        Map<Integer, Integer> goodsAllowNumber = user.getGoodsAllowNumber();

        // 今天日期
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currDate = dateFormat.format(new Date());

        List<UserBuyGoodsLimitEntity> userBuyGoodsLimitEntities = userService.listUserBuyGoodsLimitEntity(user.getUserId(), currDate);
        // 今天，用户还没有购买限购商品;
        for (GoodsLimitBuyType goodsLimitBuyType : GoodsLimitBuyType.values()) {
            goodsAllowNumber.put(goodsLimitBuyType.getGoodsId(), goodsLimitBuyType.getLimitNumber());
        }
        for (UserBuyGoodsLimitEntity goodsLimitEntity : userBuyGoodsLimitEntities) {
            Goods goods = goodsMap.get(goodsLimitEntity.getGoodsId());
            goodsAllowNumber.put(goodsLimitEntity.getGoodsId(), goods.getNumberLimit() - goodsLimitEntity.getNumber());
        }

    }


}
