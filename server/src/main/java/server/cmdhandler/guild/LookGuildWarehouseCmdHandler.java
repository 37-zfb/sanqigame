package server.cmdhandler.guild;

import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayGuild;
import server.model.User;
import server.model.props.AbstractPropsProperty;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import type.PropsType;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 * 查看仓库
 */
@Component
@Slf4j
public class LookGuildWarehouseCmdHandler implements ICmdHandler<GameMsg.LookGuildWarehouseCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.LookGuildWarehouseCmd lookGuildWarehouseCmd) {
        MyUtil.checkIsNull(ctx, lookGuildWarehouseCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayGuild playGuild = user.getPlayGuild();
        if (playGuild == null) {
            throw new CustomizeException(CustomizeErrorCode.NOT_JOIN_GUILD);
        }

        //仓库中的
        int warehouseMoney = playGuild.getWarehouseMoney();
        Map<Integer, Props> warehouseProps = playGuild.getWAREHOUSE_PROPS();

        GameMsg.LookGuildWarehouseResult.Builder newBuilder = GameMsg.LookGuildWarehouseResult.newBuilder();

        for (Map.Entry<Integer, Props> propsEntry : warehouseProps.entrySet()) {
            GameMsg.Props.Builder propsBuilder = GameMsg.Props.newBuilder();
            AbstractPropsProperty propsProperty = propsEntry.getValue().getPropsProperty();
            if (propsProperty.getType() == PropsType.Equipment) {
                Equipment equipment = (Equipment) propsProperty;
                propsBuilder.setLocation(propsEntry.getKey())
                        .setPropsId(propsEntry.getValue().getId())
                        .setDurability(equipment.getDurability())
                        .setPropsNumber(1);
            } else if (propsEntry.getValue().getPropsProperty().getType() == PropsType.Potion) {
                Potion potion = (Potion) propsProperty;
                propsBuilder.setLocation(propsEntry.getKey())
                        .setPropsId(potion.getPropsId())
                        .setPropsNumber(potion.getNumber());
            }

            newBuilder.addProps(propsBuilder);
        }
        newBuilder.setMoney(warehouseMoney);

        GameMsg.LookGuildWarehouseResult lookGuildWarehouseResult = newBuilder.build();
        ctx.writeAndFlush(lookGuildWarehouseResult);
    }
}
