package server.cmdhandler.guild;

import entity.db.CurrUserStateEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.timer.guild.DbGuildTimer;
import server.timer.state.DbUserStateTimer;
import type.GuildMemberType;
import util.MyUtil;

/**
 * @author 张丰博
 * 被踢
 */
@Component
@Slf4j
public class ModifyGuildStateCmdHandler implements ICmdHandler<GameMsg.ModifyGuildStateCmd> {

    @Autowired
    private DbUserStateTimer userStateTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.ModifyGuildStateCmd modifyGuildStateCmd) {

        MyUtil.checkIsNull(ctx,modifyGuildStateCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        CurrUserStateEntity userState = PublicMethod.getInstance().createUserState(user);
        userState.setGuildId(GuildMemberType.Public.getRoleId());
        userStateTimer.modifyUserState(userState);

        user.setPlayGuild(null);
    }
}
