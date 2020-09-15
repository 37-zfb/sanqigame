package server.async;

import com.alibaba.fastjson.JSON;
import constant.EquipmentConst;
import entity.MailProps;
import entity.db.*;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.GuildManager;
import server.model.PlayGuild;
import server.model.PlayMail;
import server.model.PlayTask;
import server.model.User;
import server.model.profession.Skill;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import server.model.store.Goods;
import server.scene.GameData;
import server.service.*;
import type.GoodsLimitBuyType;
import type.GuildMemberType;
import type.MailType;
import type.TaskType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author 张丰博
 */
@Service
@Slf4j
public class LoadResourcesService {


    @Autowired
    private UserService userService;
    @Autowired
    private MailService mailService;
    @Autowired
    private GuildService guildService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private FriendService friendService;


    public void asyn(UserEntity userEntity, ChannelHandlerContext ctx, Function<User, Void> callback) {
        if (userEntity == null || callback == null) {
            return;
        }

        AsyncLoadResources asyncLoadResources = new AsyncLoadResources(userEntity) {
            @Override
            public void doFinish() {
                if (callback != null) {
                    // 执行回调函数
                    callback.apply(this.getUser());
                }
            }
        };

        AsyncOperationProcessor.getInstance().process(asyncLoadResources, ctx);

    }

    private class AsyncLoadResources implements IAsyncOperation {

        private UserEntity userEntity;

        private User user = null;

        public AsyncLoadResources(UserEntity userEntity) {
            if (userEntity == null) {
                throw new RuntimeException("不合法的参数!");
            }
            this.userEntity = userEntity;
        }

        public User getUser() {
            return user;
        }

        /**
         * 异步执行
         */
        @Override
        public void doAsyn() {
            log.info("当前线程始:{}", Thread.currentThread().getName());

            CurrUserStateEntity userState = userService.getCurrUserStateByUserId(userEntity.getId());
            User user = createUser(userEntity, userState);
            this.user = user;
        }


        @Override
        public int getBindId() {
            return userEntity.getUserName().charAt(userEntity.getUserName().length() - 1);
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
        user.setLv(userState.getLv());
        user.setExperience(userState.getExperience());

        if (!userState.getGuildId().equals(GuildMemberType.Public.getRoleId())) {
            GuildMemberEntity memberEntity = guildService.findGuildMemberById(user.getUserId());
            if (memberEntity != null) {
                PlayGuild playGuild = GuildManager.getGuild(userState.getGuildId());
                playGuild.getGuildMemberMap().get(user.getUserId()).setOnline(true);
                user.setPlayGuild(playGuild);
            } else {
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
        loadTask(user);
        loadFriend(user);
        // 群发邮件
//        sendMailAll(user);

        // 启动定时器
//        user.startTimer();
        // 设置mp恢复结束时间
        user.resumeMpTime();

        return user;
    }

    private void loadFriend(User user) {
        List<DbFriendEntity> friendEntityList = friendService.listFriend(user.getUserId());
        Map<Integer, String> friendMap = user.getPLAY_FRIEND().getFRIEND_MAP();
        friendEntityList.forEach(f->friendMap.put(f.getUserId(),f.getFriendName()));
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
     * 加载当前任务状态
     *
     * @param user
     */
    private void loadTask(User user) {
        DbTaskEntity taskEntity = taskService.getCurrTaskById(user.getUserId());
        PlayTask playTask = user.getPlayTask();
        playTask.setHaveTask(!taskEntity.getCompletedTask().equals(TaskType.NonTask.getTaskCode()));
        if (playTask.isHaveTask()) {
            playTask.setCurrTaskId(taskEntity.getCurrTask());
            playTask.setCurrTaskCompleted(taskEntity.getCurrTaskCompleted().equals(TaskType.CurrTaskCompleted.getTaskCode()));
            playTask.setCompletedTaskId(taskEntity.getCompletedTask());
            if (taskEntity.getCurrTask().equals(2)) {
                playTask.setKillNumber(taskEntity.getTaskProcess());
            }
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
     *
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
