package server.cmdhandler.guild;

import constant.EquipmentConst;
import constant.GuildConst;
import constant.PotionConst;
import entity.db.DbGuildEquipment;
import entity.db.DbGuildPotion;
import entity.db.UserEquipmentEntity;
import entity.db.UserPotionEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayGuild;
import server.model.User;
import server.model.props.AbstractPropsProperty;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import server.timer.guild.DbGuildTimer;
import server.timer.state.DbUserStateTimer;
import type.PropsType;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 * 道具放入仓库
 */
@Component
@Slf4j
public class PutInPropsCmdHandler implements ICmdHandler<GameMsg.PutInPropsCmd> {

    @Autowired
    private DbGuildTimer guildTimer;
    @Autowired
    private DbUserStateTimer userStateTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.PutInPropsCmd putInPropsCmd) {
        MyUtil.checkIsNull(ctx, putInPropsCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayGuild playGuild = user.getPlayGuild();
        if (playGuild == null) {
            throw new CustomizeException(CustomizeErrorCode.NOT_JOIN_GUILD);
        }

        GameMsg.Props props = putInPropsCmd.getProps();
        if (props != null && user.getBackpack().get(props.getLocation()) == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_PROPS);
        }

        synchronized (playGuild.getWAREHOUSE_MONITOR()) {
            //添加道具进入仓库
            addProps(props, user);
        }

    }


    /**
     * 添加道具
     *
     * @param props
     * @param user
     */
    private void addProps(GameMsg.Props props, User user) {

        Map<Integer, Props> backpack = user.getBackpack();
        PlayGuild playGuild = user.getPlayGuild();
        Map<Integer, Props> warehouseProps = playGuild.getWAREHOUSE_PROPS();
        int location = props.getLocation();
        int propsNumber = props.getPropsNumber();

        Props p = backpack.get(location);
        AbstractPropsProperty propsProperty = p.getPropsProperty();
        if (propsProperty.getType() == PropsType.Equipment) {

            addEquipment(propsProperty, warehouseProps, playGuild, p);
            backpack.remove(location);

            // 持久化自己装备状态
            UserEquipmentEntity equipmentEntity = new UserEquipmentEntity();
            equipmentEntity.setUserId(user.getUserId());
            equipmentEntity.setLocation(location);
            userStateTimer.deleteUserEquipment(equipmentEntity);

            log.info("用户 {} 添加 {} 进仓库", user.getUserName(), p.getName());
        } else if (propsProperty.getType() == PropsType.Potion) {
            // 此道具是 药剂
            Potion potion = (Potion) propsProperty;

            addPotion(potion, propsNumber, warehouseProps, playGuild, p);

            //持久化自身道具状态
            UserPotionEntity userPotionEntity = new UserPotionEntity();
            userPotionEntity.setUserId(user.getUserId());
            userPotionEntity.setLocation(location);
            if (potion.getNumber() > propsNumber) {
                potion.setNumber(potion.getNumber() - propsNumber);
                userPotionEntity.setNumber(potion.getNumber());
                userStateTimer.modifyUserPotion(userPotionEntity);
            } else {
                backpack.remove(location);
                userStateTimer.deleteUserPotion(userPotionEntity);
            }

            log.info("用户 {} 个 {} 添加 {} 进仓库;", user.getUserName(), p.getName(),propsNumber);
        }

    }

    private void addPotion(Potion potion, Integer propsNumber, Map<Integer, Props> warehouseProps, PlayGuild playGuild, Props p) {
        if (potion.getNumber() < propsNumber) {
            throw new CustomizeException(CustomizeErrorCode.POTION_INSUFFICIENT);
        }

        DbGuildPotion dbGuildPotion = new DbGuildPotion();
        dbGuildPotion.setGuildId(playGuild.getId());
        dbGuildPotion.setPropsId(potion.getPropsId());


        boolean isExist = false;
        for (Map.Entry<Integer, Props> pro : warehouseProps.entrySet()) {
            // 查询背包中是否有该药剂
            if (potion.getPropsId().equals(pro.getValue().getId())) {
                // 判断该药剂的数量是否达到上限
                // 背包中已有该药剂
                Potion po = (Potion) pro.getValue().getPropsProperty();
                if ((po.getNumber() + propsNumber) > PotionConst.POTION_MAX_NUMBER) {
                    throw new CustomizeException(CustomizeErrorCode.PROPS_REACH_LIMIT);
                }

                po.setNumber(po.getNumber() + propsNumber);
                isExist = true;

                dbGuildPotion.setNumber(po.getNumber());
                dbGuildPotion.setLocation(pro.getKey());
                guildTimer.modifyGuildPotion(dbGuildPotion);
                break;
            }
        }
        // 背包中还没有该药剂
        Potion po = null;
        if (!isExist) {
            if (warehouseProps.size() >= GuildConst.WAREHOUSE_MAX) {
                // 此时仓库已满，
                throw new CustomizeException(CustomizeErrorCode.WAREHOUSE_SPACE_INSUFFICIENT);
            }

            dbGuildPotion.setNumber(propsNumber);

            for (int i = 1; i <= GuildConst.WAREHOUSE_MAX; i++) {
                if (!warehouseProps.keySet().contains(i)) {
                    Props pro = new Props();
                    pro.setId(potion.getPropsId());
                    pro.setName(p.getName());
                    po = new Potion(null, potion.getPropsId(), potion.getCdTime(), potion.getInfo(), potion.getResumeFigure(), potion.getPercent(), propsNumber);
                    pro.setPropsProperty(po);

                    dbGuildPotion.setLocation(i);
                    // 药剂添加进背包
                    warehouseProps.put(i, pro);

                    guildTimer.addGuildPotion(dbGuildPotion);
                    break;
                }
            }
        }
    }

    private void addEquipment(AbstractPropsProperty propsProperty, Map<Integer, Props> warehouseProps, PlayGuild playGuild, Props p) {
        Equipment equipment = (Equipment) propsProperty;
        if (warehouseProps.size() >= GuildConst.WAREHOUSE_MAX) {
            // 此时仓库已满，
            throw new CustomizeException(CustomizeErrorCode.WAREHOUSE_SPACE_INSUFFICIENT);
        }

        DbGuildEquipment dbGuildEquipment = new DbGuildEquipment();
        dbGuildEquipment.setDurability(equipment.getDurability());
        dbGuildEquipment.setGuildId(playGuild.getId());
        dbGuildEquipment.setPropsId(equipment.getPropsId());

        Equipment equ = null;
        for (int i = 1; i < GuildConst.WAREHOUSE_MAX; i++) {
            if (!warehouseProps.keySet().contains(i)) {
                Props pro = new Props();
                pro.setId(equipment.getPropsId());
                pro.setName(p.getName());
                equ = new Equipment(equipment.getId(), pro.getId(), EquipmentConst.MAX_DURABILITY, equipment.getDamage(), equipment.getEquipmentType());
                pro.setPropsProperty(equ);

                warehouseProps.put(i, pro);
                dbGuildEquipment.setLocation(i);

                break;
            }
        }
        //持久化
        guildTimer.addGuildEquipment(dbGuildEquipment);

    }

}
