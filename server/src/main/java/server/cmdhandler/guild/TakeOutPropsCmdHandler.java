package server.cmdhandler.guild;

import entity.db.DbGuildEquipment;
import entity.db.DbGuildPotion;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.cmdhandler.task.listener.TaskPublicMethod;
import server.model.PlayGuild;
import server.model.User;
import server.model.props.AbstractPropsProperty;
import server.model.props.Potion;
import server.model.props.Props;
import server.timer.guild.DbGuildTimer;
import type.PropsType;
import util.MyUtil;

/**
 * @author 张丰博
 *  从仓库中取出道具
 */
@Component
@Slf4j
public class TakeOutPropsCmdHandler implements ICmdHandler<GameMsg.TakeOutPropsCmd> {
    @Autowired
    private DbGuildTimer guildTimer;

    @Autowired
    private TaskPublicMethod taskPublicMethod;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.TakeOutPropsCmd takeOutPropsCmd) {

        MyUtil.checkIsNull(ctx, takeOutPropsCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayGuild playGuild = user.getPlayGuild();
        if (playGuild == null) {
            throw new CustomizeException(CustomizeErrorCode.NOT_JOIN_GUILD);
        }

        GameMsg.Props props = takeOutPropsCmd.getProps();
        int number = takeOutPropsCmd.getNumber();
        Props p = playGuild.getWAREHOUSE_PROPS().get(props.getLocation());
//        synchronized (playGuild.getWAREHOUSE_MONITOR()) {
//            p = playGuild.getWAREHOUSE_PROPS().get(props.getLocation());
//            if (p == null){
//                throw new CustomizeException(CustomizeErrorCode.PROPS_NOT_EXIST);
//            }
//            if (p.getPropsProperty().getType() == PropsType.Equipment){
//                //持久化装备
//                PublicMethod.getInstance().addEquipment(user, p);
//                playGuild.getWAREHOUSE_PROPS().remove(props.getLocation());
//            }else if (p.getPropsProperty().getType() == PropsType.Potion){
//                AbstractPropsProperty propsProperty = playGuild.getWAREHOUSE_PROPS().get(props.getLocation()).getPropsProperty();
//                potion = (Potion) propsProperty;
//
//                if (potion.getNumber() < number){
//                    // 仓库中道具数量不足
//                    throw new CustomizeException(CustomizeErrorCode.WAREHOUSE_POTION_NUMBER_NOT_ENOUGH);
//                }
//                //持久化药剂
//                PublicMethod.getInstance().addPotion(p, user, number);
//                //修改仓库药剂数量
//                if (potion.getNumber() == number){
//                    playGuild.getWAREHOUSE_PROPS().remove(props.getLocation());
//                }else {
//                    potion.setNumber(potion.getNumber()-number);
//                }
//
//            }
//        }
        //修改仓库
        playGuild.modifyProps(user, props, number);

        //持久化
        if (p.getPropsProperty().getType() == PropsType.Equipment){
            DbGuildEquipment dbGuildEquipment = new DbGuildEquipment();
            dbGuildEquipment.setLocation(props.getLocation());
            dbGuildEquipment.setGuildId(playGuild.getId());
            guildTimer.deleteGuildEquipment(dbGuildEquipment);
        }else if (p.getPropsProperty().getType() == PropsType.Potion){
            Potion potion = (Potion) p.getPropsProperty();

            DbGuildPotion dbGuildPotion = new DbGuildPotion();
            dbGuildPotion.setGuildId(playGuild.getId());
            dbGuildPotion.setLocation(props.getLocation());
            if (potion.getNumber() != 0){
                dbGuildPotion.setNumber(potion.getNumber());
                guildTimer.modifyGuildPotion(dbGuildPotion);
            }else {
                guildTimer.deleteGuildPotion(dbGuildPotion);
            }
        }

        log.info("用户 {} 从仓库中取出 {}", user.getUserName(),p.getName());

        taskPublicMethod.listener(user);
    }
}
