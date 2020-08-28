package server.cmdhandler.guild;

import constant.GuildConst;
import entity.db.GuildMemberEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.GuildManager;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayGuild;
import server.model.User;
import server.timer.guild.DbGuildTimer;
import type.GuildMemberType;
import util.MyUtil;

/**
 * @author 张丰博
 * 用户加入公会处理类
 */
@Component
@Slf4j
public class UserEnterGuildCmdHandler implements ICmdHandler<GameMsg.UserEnterGuildCmd> {

    @Autowired
    private DbGuildTimer guildTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserEnterGuildCmd userEnterGuildCmd) {

        MyUtil.checkIsNull(ctx, userEnterGuildCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        if (user.getPlayGuild() != null) {
            throw new CustomizeException(CustomizeErrorCode.USER_HAVE_GUILD);
        }

        int guildId = userEnterGuildCmd.getGuildId();
        PlayGuild guild = GuildManager.getGuild(guildId);

        if (guild == null) {
            // 公会不存在
            throw new CustomizeException(CustomizeErrorCode.GUILD_NOT_EXIST);
        }

        if (guild.getGuildMemberMap().size() >= GuildConst.GUILD_MAX_NUMBER) {
            //公会人数已到达上限
            throw new CustomizeException(CustomizeErrorCode.GUILD_REACH_LIMIT);
        }

        user.setPlayGuild(guild);
        GuildMemberEntity guildMemberEntity = new GuildMemberEntity();
        guildMemberEntity.setGuildId(guildId);
        guildMemberEntity.setGuildPosition(GuildMemberType.Member.getRoleId());
        guildMemberEntity.setOnline(true);
        guildMemberEntity.setUserId(user.getUserId());
        guildMemberEntity.setUserName(user.getUserName());

        guild.getGuildMemberMap().put(user.getUserId(), guildMemberEntity);

        guildTimer.addGuildMemberEntity(guildMemberEntity);
        log.info("用户 {} 加入 {} 公会", user.getUserName(), guild.getGuildEntity().getGuildName());

        GameMsg.Guild.Builder guildInfo = GameMsg.Guild.newBuilder()
                .setGuildId(guildId)
                .setGuildName(guild.getGuildEntity().getGuildName());
        GameMsg.UserEnterGuildResult userEnterGuildResult = GameMsg.UserEnterGuildResult.newBuilder()
                .setGuild(guildInfo)
                .build();
        ctx.writeAndFlush(userEnterGuildResult);
    }
}
