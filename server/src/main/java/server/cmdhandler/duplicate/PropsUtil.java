package server.cmdhandler.duplicate;

import constant.BackPackConst;
import constant.EquipmentConst;
import constant.PotionConst;
import entity.db.UserEquipmentEntity;
import entity.db.UserPotionEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import server.GameServer;
import server.cmdhandler.auction.AuctionUtil;
import server.model.User;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import server.scene.GameData;
import server.timer.state.DbUserStateTimer;
import server.util.IdWorker;
import type.PropsType;

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
     * @param propsIdList
     * @param user
     * @param newBuilder
     */
    public void addProps(List<Integer> propsIdList, User user, GameMsg.DuplicateFinishResult.Builder newBuilder) {
        if (propsIdList == null || user == null || newBuilder == null) {
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
                    location = this.addEquipment(user, props);
                    reward.setUserPropsId(((Equipment)user.getBackpack().get(location).getPropsProperty()).getId());
                } else if (props.getPropsProperty().getType() == PropsType.Potion) {
                    location = this.addPotion(props, user, 1);
                    reward.setUserPropsId(((Potion)user.getBackpack().get(location).getPropsProperty()).getId());
                }

                log.info("获得道具的id: {}", propsId);
                reward.setLocation(location);
                reward.setPropsNumber(1);
                reward.setPropsId(propsId);
            } catch (CustomizeException e) {
                log.info("获得道具失败, 道具id: {}", propsId);
                //此时给玩家发邮件
                log.error(e.getMessage(), e);
                reward = null;
                AuctionUtil.sendMailBuyer(user.getUserId(), propsId, 1, "背包已满;");
            }

            if (reward == null) {
                continue;
            }
            newBuilder.addProps(reward);
        }
    }


    /**
     * 添加装备
     *
     * @param user
     * @param props
     * @throws CustomizeException 如果背包满了，则抛出异常
     */
    public int addEquipment(User user, Props props) {

        Map<Integer, Props> backpack = user.getBackpack();

        if (backpack.size() >= BackPackConst.MAX_CAPACITY) {
            // 此时背包已满，
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

        Equipment equ = null;
        int location = 0;
        for (int i = 1; i < BackPackConst.MAX_CAPACITY; i++) {
            if (!backpack.keySet().contains(i)) {
                Props pro = new Props();
                pro.setId(equipment.getPropsId());
                pro.setName(props.getName());
                equ = new Equipment(null, pro.getId(), EquipmentConst.MAX_DURABILITY, equipment.getDamage(), equipment.getEquipmentType());
                pro.setPropsProperty(equ);

                backpack.put(i, pro);
                location = i;
                userEquipmentEntity.setLocation(i);
                break;
            }
        }
        userEquipmentEntity.setId(IdWorker.generateId());
        equ.setId(userEquipmentEntity.getId());

        userStateTimer.addUserEquipment(userEquipmentEntity);

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
    public int addPotion(Props props, User user, Integer number) {

        Map<Integer, Props> backpack = user.getBackpack();

        // 此道具是 药剂
        Potion potion = (Potion) props.getPropsProperty();

        UserPotionEntity userPotionEntity = new UserPotionEntity();
        userPotionEntity.setUserId(user.getUserId());
        userPotionEntity.setPropsId(potion.getPropsId());

        int location = 0;
        boolean isExist = false;
        for (Map.Entry<Integer, Props> pro : backpack.entrySet()) {
            // 查询背包中是否有该药剂
            if (potion.getPropsId().equals(pro.getValue().getId())) {
                // 判断该药剂的数量是否达到上限
                // 背包中已有该药剂
                Potion po = (Potion) pro.getValue().getPropsProperty();
                if ((po.getNumber() + number) > PotionConst.POTION_MAX_NUMBER) {
                    throw new CustomizeException(CustomizeErrorCode.PROPS_REACH_LIMIT);
                }

                po.setNumber(po.getNumber() + number);
                isExist = true;

                location = pro.getKey();

                userPotionEntity.setNumber(po.getNumber());
//                userPotionEntity.setLocation(pro.getKey());
                userPotionEntity.setId(po.getId());
                userStateTimer.modifyUserPotion(userPotionEntity);
                break;
            }
        }
        // 背包中还没有该药剂
        if (!isExist) {
            if (backpack.size() >= BackPackConst.MAX_CAPACITY) {
                // 此时背包已满，
                throw new CustomizeException(CustomizeErrorCode.BACKPACK_SPACE_INSUFFICIENT);
            }

            userPotionEntity.setNumber(number);

            for (int i = 1; i < BackPackConst.MAX_CAPACITY; i++) {
                if (!backpack.keySet().contains(i)) {
                    Props pro = new Props();
                    pro.setId(potion.getPropsId());
                    pro.setName(props.getName());
                    Potion po = new Potion(null, potion.getPropsId(), potion.getCdTime(), potion.getInfo(), potion.getResumeFigure(), potion.getPercent(), number);
                    pro.setPropsProperty(po);

                    userPotionEntity.setLocation(i);
                    // 药剂添加进背包
                    backpack.put(i, pro);
                    userPotionEntity.setId(IdWorker.generateId());
                    po.setId(userPotionEntity.getId());

                    location = i;
                    userStateTimer.addUserPotion(userPotionEntity);
                    break;
                }
            }
        }
        return location;
    }



}
