package server.cmdhandler.guild;

import entity.db.CurrUserStateEntity;
import entity.db.GuildMemberEntity;
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
import server.model.UserManager;
import server.timer.guild.DbGuildTimer;
import server.timer.state.DbUserStateTimer;
import util.MyUtil;

/**
 * @author 张丰博
 * 解散公会处理类
 */
@Component
@Slf4j
public class UserDissolveGuildCmdHandler implements ICmdHandler<GameMsg.UserDissolveGuildCmd> {

    @Autowired
    private DbGuildTimer guildTimer;

    @Autowired
    private DbUserStateTimer userStateTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserDissolveGuildCmd userDissolveGuildCmd) {

        MyUtil.checkIsNull(ctx, userDissolveGuildCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        if (user.getPlayGuild() == null
                || user.getPlayGuild().getGuildEntity().getPresidentId() != user.getUserId()) {
            throw new CustomizeException(CustomizeErrorCode.USER_NO_HAVE_GUILD_OR_NOT_PRESIDENT);
        }

        GameMsg.UserDissolveGuildResult.Builder newBuilder = GameMsg.UserDissolveGuildResult.newBuilder();

        PlayGuild playGuild = user.getPlayGuild();
        guildTimer.deleteGuildEntity(playGuild.getGuildEntity());
        guildTimer.deleteGuildMemberEntity(playGuild.getGuildMemberMap());

        log.info("用户: {} 解散 公会: {}", user.getUserName(),playGuild.getGuildEntity().getGuildName());

        //改变所有公会用户的公会状态,并通知在线用户
        for (GuildMemberEntity guildMemberEntity : playGuild.getGuildMemberMap().values()) {
            userStateTimer.addModifyGuildStateSet(guildMemberEntity.getUserId());
            if (guildMemberEntity.isOnline()) {
                User guildMember = UserManager.getUserById(guildMemberEntity.getUserId());
                GameMsg.UserDissolveGuildResult build = newBuilder.setUserId(guildMember.getUserId()).build();
                guildMember.setPlayGuild(null);
                guildMember.getCtx().writeAndFlush(build);
            }
        }

    }


}
