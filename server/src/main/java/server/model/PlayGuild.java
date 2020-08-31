package server.model;

import entity.db.GuildEntity;
import entity.db.GuildMemberEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import msg.GameMsg;
import server.PublicMethod;
import server.model.props.AbstractPropsProperty;
import server.model.props.Potion;
import server.model.props.Props;
import type.PropsType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author 张丰博
 */
@Setter
@Getter
@NoArgsConstructor
@ToString
public class PlayGuild {


    /**
     * 公会id
     */
    private Integer id;

    /**
     * 公会基础信息
     */
    private GuildEntity guildEntity;

    /**
     * 用户id  公会成员
     */
    private final Map<Integer, GuildMemberEntity> guildMemberMap = new ConcurrentHashMap<>();


    /**
     * 仓库监视器
     */
    private final Object WAREHOUSE_MONITOR = new Object();
    /**
     * 仓库金币
     */
    private int warehouseMoney = 0;
    /**
     * 仓库道具  位置 道具
     */
    private final Map<Integer, Props> WAREHOUSE_PROPS = new HashMap<>();


    /**
     * 修改仓库和用户道具
     * @param user
     * @param props
     * @param number
     */
    public void modifyProps(User user, GameMsg.Props props, Integer number) {
        synchronized (this.getWAREHOUSE_MONITOR()) {
            Props p = this.getWAREHOUSE_PROPS().get(props.getLocation());
            if (p == null) {
                throw new CustomizeException(CustomizeErrorCode.PROPS_NOT_EXIST);
            }
            if (p.getPropsProperty().getType() == PropsType.Equipment) {
                //持久化装备
                PublicMethod.getInstance().addEquipment(user, p);
                this.getWAREHOUSE_PROPS().remove(props.getLocation());
            } else if (p.getPropsProperty().getType() == PropsType.Potion) {
                AbstractPropsProperty propsProperty = this.getWAREHOUSE_PROPS().get(props.getLocation()).getPropsProperty();
                Potion potion = (Potion) propsProperty;

                if (potion.getNumber() < number) {
                    // 仓库中道具数量不足
                    throw new CustomizeException(CustomizeErrorCode.WAREHOUSE_POTION_NUMBER_NOT_ENOUGH);
                }
                //持久化药剂
                PublicMethod.getInstance().addPotion(p, user, number);
                //修改仓库药剂数量
                if (potion.getNumber() == number) {
                    potion.setNumber(0);
                    this.getWAREHOUSE_PROPS().remove(props.getLocation());
                } else {
                    potion.setNumber(potion.getNumber() - number);
                }

            }
        }
    }

}
