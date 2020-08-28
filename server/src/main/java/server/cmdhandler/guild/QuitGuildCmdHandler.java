package server.cmdhandler.guild;

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
import type.GuildMemberType;
import util.MyUtil;

/**
 * @author 张丰博
 * 用户退出公会处理类
 */
@Component
@Slf4j
public class QuitGuildCmdHandler implements ICmdHandler<GameMsg.QuitGuildCmd> {

    @Autowired
    private DbGuildTimer guildTimer;
    @Autowired
    private DbUserStateTimer userStateTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.QuitGuildCmd quitGuildCmd) {

        MyUtil.checkIsNull(ctx, quitGuildCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        if (user.getPlayGuild() == null) {
            //未加入公会
            throw new CustomizeException(CustomizeErrorCode.NOT_JOIN_GUILD);
        }

        PlayGuild playGuild = user.getPlayGuild();
        GuildMemberEntity guildMemberEntity = playGuild.getGuildMemberMap().get(user.getUserId());
        playGuild.getGuildMemberMap().remove(user.getUserId());
        user.setPlayGuild(null);

        guildTimer.deleteGuildMemberEntity(guildMemberEntity);
        userStateTimer.addModifyGuildStateSet(user.getUserId());
        log.info("用户 {} 退出 {} 公会;", user.getUserName(),playGuild.getGuildEntity().getGuildName());

        GameMsg.QuitGuildResult quitGuildResult = GameMsg.QuitGuildResult.newBuilder()
                .setUserName(user.getUserName())
                .build();
        ctx.writeAndFlush(quitGuildResult);

        // 通知 会长和副会长
        for (GuildMemberEntity memberEntity : playGuild.getGuildMemberMap().values()) {
            if (!memberEntity.isOnline()){
                continue;
            }
            if (memberEntity.getGuildPosition().equals(GuildMemberType.VicePresident.getRoleId())
                    || memberEntity.getGuildPosition().equals(GuildMemberType.President.getRoleId())){
                User userById = UserManager.getUserById(memberEntity.getUserId());
                userById.getCtx().writeAndFlush(quitGuildResult);
            }
        }

    }
}
