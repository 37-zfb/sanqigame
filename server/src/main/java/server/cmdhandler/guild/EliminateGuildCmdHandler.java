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
import type.GuildMemberType;
import util.MyUtil;

/**
 * @author 张丰博
 * 踢人
 */
@Component
@Slf4j
public class EliminateGuildCmdHandler implements ICmdHandler<GameMsg.EliminateGuildCmd> {

    @Autowired
    private DbGuildTimer guildTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.EliminateGuildCmd eliminateGuildCmd) {

        MyUtil.checkIsNull(ctx,eliminateGuildCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayGuild playGuild = user.getPlayGuild();
        if (playGuild == null) {
            //未加入公会
            throw new CustomizeException(CustomizeErrorCode.NOT_JOIN_GUILD);
        }

        //被踢用户id
        int userId = eliminateGuildCmd.getUserId();
        GuildMemberEntity targetMember = playGuild.getGuildMemberMap().get(userId);
        if (targetMember == null) {
            //目标成员不在该公会
            throw new CustomizeException(CustomizeErrorCode.NO_HAVE_MEMBER);
        }

        GuildMemberEntity srcMember = playGuild.getGuildMemberMap().get(user.getUserId());
        // 会长
        if (srcMember.getGuildPosition().equals(GuildMemberType.President.getRoleId())
                && userId != GuildMemberType.President.getRoleId()){
            guildTimer.deleteGuildMemberEntity(targetMember);
            playGuild.getGuildMemberMap().remove(userId);
        }
        //副会长
        if (srcMember.getGuildPosition().equals(GuildMemberType.VicePresident.getRoleId())
                && userId != GuildMemberType.VicePresident.getRoleId()
                && userId != GuildMemberType.President.getRoleId()){
            guildTimer.deleteGuildMemberEntity(targetMember);
            playGuild.getGuildMemberMap().remove(userId);
        }

        if (targetMember.isOnline()){
            //目标用户在线
            GameMsg.EliminateGuildResult eliminateGuildResult = GameMsg.EliminateGuildResult
                    .newBuilder()
                    .build();
            UserManager.getUserById(targetMember.getUserId()).getCtx().writeAndFlush(eliminateGuildResult);
        }


    }
}
