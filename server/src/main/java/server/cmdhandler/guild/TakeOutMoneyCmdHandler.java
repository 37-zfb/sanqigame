package server.cmdhandler.guild;

import entity.db.CurrUserStateEntity;
import entity.db.GuildEntity;
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
import server.timer.guild.DbGuildTimer;
import server.timer.state.DbUserStateTimer;
import util.MyUtil;

/**
 * @author 张丰博
 * 从仓库中取出 金币
 */
@Component
@Slf4j
public class TakeOutMoneyCmdHandler implements ICmdHandler<GameMsg.TakeOutMoneyCmd> {

    @Autowired
    private DbGuildTimer guildTimer;

    @Autowired
    private DbUserStateTimer userStateTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.TakeOutMoneyCmd takeOutCmd) {

        MyUtil.checkIsNull(ctx,takeOutCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayGuild playGuild = user.getPlayGuild();
        if (playGuild == null) {
            throw new CustomizeException(CustomizeErrorCode.NOT_JOIN_GUILD);
        }

        int money = takeOutCmd.getMoney();

        synchronized (playGuild.getWAREHOUSE_MONITOR()){
            int warehouseMoney = playGuild.getWarehouseMoney();

            if (warehouseMoney < money){
                //没有那么多钱
                throw new CustomizeException(CustomizeErrorCode.WAREHOUSE_NO_MONEY);
            }
            playGuild.setWarehouseMoney(warehouseMoney-money);
        }

        log.info("用户 {} 从仓库中取出，{} 金币;", user.getUserName(),money);
        //更新仓库金币
        GuildEntity guildEntity = new GuildEntity();
        guildEntity.setId(playGuild.getGuildEntity().getId());
        guildEntity.setMoney(playGuild.getWarehouseMoney());
        guildTimer.modifyGuildEntity(guildEntity);

        user.setMoney(user.getMoney()+ money);

        //更新用户状态
        CurrUserStateEntity userState = PublicMethod.getInstance().createUserState(user);
        userStateTimer.modifyUserState(userState);

        GameMsg.TakeOutMoneyResult takeOutMoneyResult = GameMsg.TakeOutMoneyResult.newBuilder()
                .setMoney(money)
                .build();
        ctx.writeAndFlush(takeOutMoneyResult);
    }
}
