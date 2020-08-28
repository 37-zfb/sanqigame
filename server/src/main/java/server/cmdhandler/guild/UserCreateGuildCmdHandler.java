package server.cmdhandler.guild;

import constant.GuildConst;
import entity.db.GuildEntity;
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
 * 创建公会
 */
@Component
@Slf4j
public class UserCreateGuildCmdHandler implements ICmdHandler<GameMsg.UserCreateGuildCmd> {

    @Autowired
    private DbGuildTimer guildTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserCreateGuildCmd userCreateGuildCmd) {

        MyUtil.checkIsNull(ctx, userCreateGuildCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        if (user.getPlayGuild() != null) {
            //已有公会
            throw new CustomizeException(CustomizeErrorCode.USER_HAVE_GUILD);
        }
        if (user.getMoney() < GuildConst.CREATE_GUILD_MONEY) {
            // 金币不够
            throw new CustomizeException(CustomizeErrorCode.USER_MONEY_NOT_ENOUGH);
        }

        String guildName = userCreateGuildCmd.getGuildName();
        if (GuildManager.isGuildNameDuplicate(guildName)){
            // 公会名称是否存在
            throw new CustomizeException(CustomizeErrorCode.GUILD_ALREADY_EXIST);
        }

        PlayGuild playGuild = new PlayGuild();
        Integer guildId = GuildManager.addGuild(playGuild);
        log.info("当前公会id {}",guildId);

        GuildEntity guildEntity = new GuildEntity();
        guildEntity.setGuildName(guildName);
        guildEntity.setPresidentId(user.getUserId());
        guildEntity.setId(guildId);

        GuildMemberEntity guildMemberEntity = new GuildMemberEntity();
        guildMemberEntity.setGuildId(guildId);
        guildMemberEntity.setUserId(user.getUserId());
        guildMemberEntity.setGuildPosition(GuildMemberType.President.getRoleId());
        guildMemberEntity.setUserName(user.getUserName());
        guildMemberEntity.setOnline(true);

        playGuild.setGuildEntity(guildEntity);
        playGuild.getGuildMemberMap().put(user.getUserId(),guildMemberEntity);

        user.setPlayGuild(playGuild);
        user.setMoney(user.getMoney()-GuildConst.CREATE_GUILD_MONEY);
        guildTimer.addGuildEntity(guildEntity);
        guildTimer.addGuildMemberEntity(guildMemberEntity);

        log.info("用户 {} 创建了公会;", user.getUserName());
        GameMsg.UserCreateGuildResult userCreateGuildResult = GameMsg.UserCreateGuildResult.newBuilder()
                .setGuildName(guildName)
                .build();
        ctx.writeAndFlush(userCreateGuildResult);
    }
}
