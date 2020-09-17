package server.cmdhandler;

import com.alibaba.fastjson.JSON;
import constant.EquipmentConst;
import constant.MailConst;
import entity.MailProps;
import entity.db.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import server.GuildManager;
import server.async.LoadResourcesService;
import server.async.LoginService;
import server.cmdhandler.auction.AuctionUtil;
import server.cmdhandler.mail.MailUtil;
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
import server.service.TaskService;
import server.service.UserService;
import server.Broadcast;
import type.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户登录
 *
 * @author 张丰博
 */
@Slf4j
@Component
public class UserLoginCmdHandler implements ICmdHandler<GameMsg.UserLoginCmd> {

    @Autowired
    private LoginService loginService;
    @Autowired
    private LoadResourcesService loadResourcesService;


    /**
     * 用户登陆状态字典, 防止用户连点登陆按钮
     */
    private static final Map<String, Long> USER_LOGIN_STATE_MAP = new ConcurrentHashMap<>();

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserLoginCmd cmd) {

        if (ctx == null || cmd == null) {
            return;
        }

        String userName = cmd.getUserName();
        String password = cmd.getPassword();

        log.info("用户登陆:{}", userName);
        log.info("当前线程:{}", Thread.currentThread().getName());

        //若用户已存在则不能登录
        for (User user : UserManager.listUser()) {
            if (user.getUserName().equals(userName)) {
                throw new CustomizeException(CustomizeErrorCode.USER_ALREADY_LOGIN);
            }
        }

        clearTimeoutLoginTime(USER_LOGIN_STATE_MAP);

        if (USER_LOGIN_STATE_MAP.containsKey(userName)) {
            // 已点击登录
            return;
        }

        //设置用户登录时间
        USER_LOGIN_STATE_MAP.put(userName, System.currentTimeMillis());

        loginService.asyn(userName, password, ctx, (userEntity) -> {

            log.info("当前线程 = {}", Thread.currentThread().getName());
            if (userEntity == null) {
                log.error(
                        "用户登录失败,username = {}"
                        , userName
                );
                throw new CustomizeException(CustomizeErrorCode.USER_NOT_FOUND);
            }
            if (!userEntity.getPassword().equals(password)) {
                log.error("用户登录失败,username = {}", cmd.getUserName());
                throw new CustomizeException(CustomizeErrorCode.USER_NOT_FOUND);
            }

            log.info(
                    "用户登录成功，userName = {},password = {}",
                    userEntity.getUserName(),
                    userEntity.getPassword()
            );

            // 需要继续回调
            // 加载资源
            loadResourcesService.asyn(userEntity, ctx, (user) -> {
                //移除用户登录状态
                USER_LOGIN_STATE_MAP.remove(userName);

                // 添加管道
                Broadcast.addChannel(user.getCurSceneId(), ctx.channel());
                // 将用户id附着到channel中
                ctx.channel().attr(AttributeKey.valueOf("userId")).set(userEntity.getId());
                UserManager.addUser(user);
                user.setCtx(ctx);

                GameMsg.UserLoginResult.Builder resultBuilder = GameMsg.UserLoginResult.newBuilder();
                // 用户基本信息
                resultBuilder.setUserId(user.getUserId())
                        .setUserName(user.getUserName())
                        .setHp(user.getCurrHp())
                        .setMp(user.getCurrMp())
                        .setCurrSceneId(user.getCurSceneId())
                        .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                        .setProfessionId(user.getProfessionId())
                        .setMoney(user.getMoney())
                        .setLv(user.getLv());

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

                //封装任务状态
                packageTask(user, resultBuilder);

                packageFriend(user, resultBuilder);

                GameMsg.UserLoginResult userLoginResult = resultBuilder.build();
                ctx.writeAndFlush(userLoginResult);

                //发送邮件
//                MailUtil.getMailUtil().sendMail(user.getUserId(), 2000, "登录奖励;",new ArrayList<>());
                return null;
            });
            return null;
        });

    }

    private void clearTimeoutLoginTime(Map<String, Long> userLoginStateMap) {
        if (userLoginStateMap == null || userLoginStateMap.isEmpty()) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        Iterator<String> iterator = userLoginStateMap.keySet().iterator();
        while (iterator.hasNext()) {
            String userName = iterator.next();
            Long loginTime = userLoginStateMap.get(userName);

            if (loginTime == null || currentTime - loginTime > 5000) {
                iterator.remove();
            }

        }

    }

    private void packageFriend(User user, GameMsg.UserLoginResult.Builder resultBuilder) {
        Map<Integer, String> friendMap = user.getPLAY_FRIEND().getFRIEND_MAP();
        friendMap.forEach((uId, uName) -> resultBuilder.addFriend(
                GameMsg.Friend.newBuilder()
                        .setFriendId(uId)
                        .setFriendName(uName)
        ));

    }

    /**
     * 封装任务
     *
     * @param user
     * @param resultBuilder
     */
    private void packageTask(User user, GameMsg.UserLoginResult.Builder resultBuilder) {
        PlayTask playTask = user.getPlayTask();
        resultBuilder.setIsHaveTask(playTask.isHaveTask());
        if (playTask.isHaveTask()) {
            resultBuilder.setCurrTaskId(playTask.getCurrTaskId());
            resultBuilder.setCurrTaskCompleted(playTask.isCurrTaskCompleted());
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
                propsResult.setDurability(equipment.getDurability()).setUserPropsId(equipment.getId());
            } else if (propsProperty.getType() == PropsType.Potion) {
                Potion potion = (Potion) propsProperty;
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


}
