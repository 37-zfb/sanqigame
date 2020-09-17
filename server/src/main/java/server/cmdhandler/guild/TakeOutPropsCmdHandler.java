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
import server.cmdhandler.duplicate.PropsUtil;
import server.cmdhandler.task.listener.TaskPublicMethod;
import server.model.PlayGuild;
import server.model.User;
import server.model.props.AbstractPropsProperty;
import server.model.props.Potion;
import server.model.props.Props;
import server.timer.guild.DbGuildTimer;
import type.PropsType;
import util.MyUtil;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author 张丰博
 * 从仓库中取出道具
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

        GameMsg.TakeOutPropsResult.Builder newBuilder = GameMsg.TakeOutPropsResult.newBuilder();
        GameMsg.Props props = takeOutPropsCmd.getProps();
        int number = takeOutPropsCmd.getNumber();

        //修改仓库
        Props p = modifyProps(user, props, number, newBuilder);

        //持久化
        if (p.getPropsProperty().getType() == PropsType.Equipment) {
            DbGuildEquipment dbGuildEquipment = new DbGuildEquipment();
            dbGuildEquipment.setLocation(props.getLocation());
            dbGuildEquipment.setGuildId(playGuild.getId());
            guildTimer.deleteGuildEquipment(dbGuildEquipment);
        } else if (p.getPropsProperty().getType() == PropsType.Potion) {
            Potion potion = (Potion) p.getPropsProperty();

            DbGuildPotion dbGuildPotion = new DbGuildPotion();
            dbGuildPotion.setGuildId(playGuild.getId());
            dbGuildPotion.setLocation(props.getLocation());
            if (potion.getNumber() != 0) {
                dbGuildPotion.setNumber(potion.getNumber());
                guildTimer.modifyGuildPotion(dbGuildPotion);
            } else {
                guildTimer.deleteGuildPotion(dbGuildPotion);
            }
        }

        log.info("用户 {} 从仓库中取出 {}", user.getUserName(), p.getName());

        GameMsg.TakeOutPropsResult takeOutPropsResult = newBuilder
                .setNumber(number)
                .build();
        ctx.writeAndFlush(takeOutPropsResult);

        taskPublicMethod.listener(user);
    }

    /**
     * 修改仓库和用户道具
     *
     * @param user
     * @param props
     * @param number
     */
    private Props modifyProps(User user, GameMsg.Props props, Integer number, GameMsg.TakeOutPropsResult.Builder newBuilder) {
        PlayGuild playGuild = user.getPlayGuild();
        Props p;
        synchronized (playGuild.getWAREHOUSE_MONITOR()) {
            p = playGuild.getWAREHOUSE_PROPS().get(props.getLocation());
            if (p == null) {
                throw new CustomizeException(CustomizeErrorCode.PROPS_NOT_EXIST);
            }
            if (p.getPropsProperty().getType() == PropsType.Equipment) {
                //持久化装备
                PropsUtil.getPropsUtil().addProps(Collections.singletonList(p.getId()), user, newBuilder, number);
                playGuild.getWAREHOUSE_PROPS().remove(props.getLocation());
            } else if (p.getPropsProperty().getType() == PropsType.Potion) {
                AbstractPropsProperty propsProperty = playGuild.getWAREHOUSE_PROPS().get(props.getLocation()).getPropsProperty();

                Potion potion = (Potion) propsProperty;
                if (potion.getNumber() < number) {
                    // 仓库中道具数量不足
                    throw new CustomizeException(CustomizeErrorCode.WAREHOUSE_POTION_NUMBER_NOT_ENOUGH);
                }

                //持久化药剂
                PropsUtil.getPropsUtil().addProps(Collections.singletonList(p.getId()), user, newBuilder, number);
                //修改仓库药剂数量
                if (potion.getNumber() == number) {
                    potion.setNumber(0);
                    playGuild.getWAREHOUSE_PROPS().remove(props.getLocation());
                } else {
                    potion.setNumber(potion.getNumber() - number);
                }

            }
        }
        return p;
    }


}
