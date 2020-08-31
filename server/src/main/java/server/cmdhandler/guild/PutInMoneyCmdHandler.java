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
 * 放进仓库 金币
 */
@Component
@Slf4j
public class PutInMoneyCmdHandler implements ICmdHandler<GameMsg.PutInMoneyCmd> {

    @Autowired
    private DbGuildTimer guildTimer;

    @Autowired
    private DbUserStateTimer userStateTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.PutInMoneyCmd putInCmd) {
        MyUtil.checkIsNull(ctx, putInCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayGuild playGuild = user.getPlayGuild();
        if (playGuild == null) {
            throw new CustomizeException(CustomizeErrorCode.NOT_JOIN_GUILD);
        }

        int money = putInCmd.getMoney();
        if (user.getMoney() < money) {
            //金币不足
            throw new CustomizeException(CustomizeErrorCode.USER_MONEY_NOT_ENOUGH);
        }

        synchronized (playGuild.getWAREHOUSE_MONITOR()) {
            playGuild.setWarehouseMoney(playGuild.getWarehouseMoney() + money);
        }

        //更新仓库金币
        GuildEntity guildEntity = new GuildEntity();
        guildEntity.setId(playGuild.getGuildEntity().getId());
        guildEntity.setMoney(playGuild.getWarehouseMoney());
        guildTimer.modifyGuildEntity(guildEntity);


        log.info("用户 {} 放入公会仓库 {} 金币;", user.getUserName(), money);
        user.setMoney(user.getMoney() - money);

        //更新用户状态
        CurrUserStateEntity userState = PublicMethod.getInstance().createUserState(user);
        userStateTimer.modifyUserState(userState);

    }
}
