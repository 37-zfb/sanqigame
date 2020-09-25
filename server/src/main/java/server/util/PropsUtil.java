package server.util;

import com.google.protobuf.GeneratedMessageV3;
import constant.BackPackConst;
import constant.EquipmentConst;
import constant.PotionConst;
import entity.MailProps;
import entity.db.UserEquipmentEntity;
import entity.db.UserPotionEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import server.GameServer;
import server.cmdhandler.mail.MailUtil;
import server.model.User;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import server.scene.GameData;
import server.timer.state.DbUserStateTimer;
import type.PropsType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 */
@Slf4j
public final class PropsUtil {

    private static final PropsUtil PROPS_UTIL = new PropsUtil();

    private DbUserStateTimer userStateTimer = GameServer.APPLICATION_CONTEXT.getBean(DbUserStateTimer.class);

    private PropsUtil() {
    }

    public static PropsUtil getPropsUtil() {
        return PROPS_UTIL;
    }

    /**
     * 添加道具
     *
     * @param propsIdList
     * @param user
     * @param newBuilder
     * @param number      数量
     */
    public void addProps(List<Integer> propsIdList, User user, GeneratedMessageV3.Builder newBuilder, Integer number) {
        if (propsIdList == null || user == null) {
            return;
        }

        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        for (Integer propsId : propsIdList) {
            if (propsId == null) {
                continue;
            }

            Props props = propsMap.get(propsId);
            if (props == null) {
                continue;
            }

            GameMsg.Props.Builder reward = GameMsg.Props.newBuilder();
            try {

                int location = 0;
                if (props.getPropsProperty().getType() == PropsType.Equipment) {
                    // 添加装备到数据库;  条件不满足时有异常抛出
                    location = this.addEquipment(user, props, newBuilder);

                } else if (props.getPropsProperty().getType() == PropsType.Potion) {
                    location = this.addPotion(props, user, number, newBuilder);
                }

                log.info("获得道具的id: {}", propsId);
                reward.setLocation(location);
                reward.setPropsNumber(number);
                reward.setPropsId(propsId);
            } catch (CustomizeException e) {
                log.info("获得道具失败, 道具id: {}", propsId);
                //此时给玩家发邮件
                log.error(e.getMessage(), e);

            }
        }
    }


    /**
     * 添加装备
     *
     * @param user
     * @param props
     * @throws CustomizeException 如果背包满了，则抛出异常
     */
    public int addEquipment(User user, Props props, GeneratedMessageV3.Builder newBuilder) {

        Map<Integer, Props> backpack = user.getBackpack();
        GameMsg.Props.Builder reward = GameMsg.Props.newBuilder();

        if (backpack.size() >= BackPackConst.MAX_CAPACITY) {
            // 此时背包已满，
            MailUtil.getMailUtil().sendMail(user.getUserId(),
                    0,
                    "背包已满",
                    Collections.singletonList(new MailProps(props.getId(), 1)));
            throw new CustomizeException(CustomizeErrorCode.BACKPACK_SPACE_INSUFFICIENT);
        }

        // 此道具是 装备
        Equipment equipment = (Equipment) props.getPropsProperty();

        //封装
        UserEquipmentEntity userEquipmentEntity = new UserEquipmentEntity();
        userEquipmentEntity.setIsWear(EquipmentConst.NO_WEAR);
        userEquipmentEntity.setDurability(EquipmentConst.MAX_DURABILITY);
        userEquipmentEntity.setPropsId(equipment.getPropsId());
        userEquipmentEntity.setUserId(user.getUserId());
        userEquipmentEntity.setId(IdWorker.generateId());

        Equipment equ = null;
        int location = 0;
        for (int i = 1; i < BackPackConst.MAX_CAPACITY; i++) {
            if (!backpack.containsKey(i)) {
                Props pro = new Props();
                pro.setId(equipment.getPropsId());
                pro.setName(props.getName());
                equ = new Equipment(userEquipmentEntity.getId(), pro.getId(), EquipmentConst.MAX_DURABILITY, equipment.getDamage(), equipment.getEquipmentType());
                pro.setPropsProperty(equ);

                backpack.put(i, pro);
                location = i;
                userEquipmentEntity.setLocation(i);
                break;
            }
        }

        userStateTimer.addUserEquipment(userEquipmentEntity);

        reward.setUserPropsId(equ.getId());
        reward.setDurability(equ.getDurability());
        reward.setLocation(location);
        reward.setPropsNumber(1);
        reward.setPropsId(props.getId());

        addReward(newBuilder, reward);

        return location;
    }

    /**
     * 添加药剂
     *
     * @param props
     * @param user
     * @param number
     * @throws CustomizeException 如果背包中不存在此药剂且背包满了，则抛出异常；背包中此药剂数量达到上限抛出异常
     */
    public int addPotion(Props props, User user, Integer number, GeneratedMessageV3.Builder newBuilder) {
        Map<Integer, Props> backpack = user.getBackpack();

        // 此道具是 药剂
        Potion potion = (Potion) props.getPropsProperty();

        int location = 0;
        boolean isExist = false;
        for (Map.Entry<Integer, Props> pro : backpack.entrySet()) {

            // 查询背包中是否有该药剂
            if (potion.getPropsId().equals(pro.getValue().getId())) {

                UserPotionEntity userPotionEntity = new UserPotionEntity();
                userPotionEntity.setUserId(user.getUserId());
                userPotionEntity.setPropsId(potion.getPropsId());
                GameMsg.Props.Builder reward = GameMsg.Props.newBuilder();

                // 判断该药剂的数量是否达到上限
                // 背包中已有该药剂
                Potion po = (Potion) pro.getValue().getPropsProperty();

                int i = PotionConst.POTION_MAX_NUMBER - po.getNumber();
                if (number > i) {
                    //此时需要开辟新的一格
                    po.setNumber(po.getNumber() + i);
                    number = number - i;
                } else {
                    //此时该格子刚好达到上限或还没达到上限
                    po.setNumber(po.getNumber() + number);
                    isExist = true;
                }

                location = pro.getKey();

                userPotionEntity.setNumber(po.getNumber());
                userPotionEntity.setId(po.getId());
                userStateTimer.modifyUserPotion(userPotionEntity);

                reward.setUserPropsId(po.getId());
                reward.setLocation(location);
                reward.setPropsId(props.getId());
                reward.setPropsNumber(po.getNumber());

                addReward(newBuilder, reward);
            }
        }

        // 背包中还没有该药剂
        if (!isExist) {
            if (backpack.size() >= BackPackConst.MAX_CAPACITY) {
                // 此时背包已满，
                MailUtil.getMailUtil().sendMail(user.getUserId(),
                        0,
                        "背包已满",
                        Collections.singletonList(new MailProps(props.getId(), number)));
                throw new CustomizeException(CustomizeErrorCode.BACKPACK_SPACE_INSUFFICIENT);
            }

            for (int i = 1; i < BackPackConst.MAX_CAPACITY; i++) {
                GameMsg.Props.Builder reward = GameMsg.Props.newBuilder();

                if (!backpack.containsKey(i)) {
                    UserPotionEntity userPotionEntity = new UserPotionEntity();
                    userPotionEntity.setUserId(user.getUserId());
                    userPotionEntity.setPropsId(potion.getPropsId());

                    userPotionEntity.setId(IdWorker.generateId());

                    Props pro = new Props();
                    pro.setId(potion.getPropsId());
                    pro.setName(props.getName());
                    Potion po = new Potion(userPotionEntity.getId(), potion.getPropsId(), potion.getCdTime(), potion.getInfo(), potion.getResumeFigure(), potion.getPercent());
                    if (number > PotionConst.POTION_MAX_NUMBER) {
                        po.setNumber(PotionConst.POTION_MAX_NUMBER);
                        userPotionEntity.setNumber(PotionConst.POTION_MAX_NUMBER);
                        number = number - PotionConst.POTION_MAX_NUMBER;
                        reward.setPropsNumber(PotionConst.POTION_MAX_NUMBER);
                    } else {
                        po.setNumber(number);
                        userPotionEntity.setNumber(number);
                        reward.setPropsNumber(number);
                        number = 0;
                    }

                    pro.setPropsProperty(po);

                    userPotionEntity.setLocation(i);
                    // 药剂添加进背包
                    backpack.put(i, pro);

                    location = i;
                    userStateTimer.addUserPotion(userPotionEntity);

                    reward.setUserPropsId(po.getId());
                    reward.setLocation(location);
                    reward.setPropsId(props.getId());

                    addReward(newBuilder, reward);

                    if (number == 0) {
                        break;
                    }
                }
            }
        }
        return location;
    }


    private void addReward(GeneratedMessageV3.Builder newBuilder, GameMsg.Props.Builder reward) {
        if (newBuilder == null || reward == null) {
            return;
        }

        try {
            Method addProps = newBuilder.getClass().getMethod("addProps", GameMsg.Props.Builder.class);
            addProps.invoke(newBuilder, reward);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(e.getMessage(), e);
        }
    }

}
