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
 * 任命职位
 */
@Component
@Slf4j
public class AppointMemberCmdHandler implements ICmdHandler<GameMsg.AppointMemberCmd> {

    @Autowired
    private DbGuildTimer guildTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.AppointMemberCmd appointMemberCmd) {

        MyUtil.checkIsNull(ctx, appointMemberCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayGuild playGuild = user.getPlayGuild();
        if (playGuild == null) {
            //未加入公会
            throw new CustomizeException(CustomizeErrorCode.NOT_JOIN_GUILD);
        }


        int positionId = appointMemberCmd.getPositionId();
        int userId = appointMemberCmd.getUserId();

        // 目标成员
        GuildMemberEntity targetMember = playGuild.getGuildMemberMap().get(userId);
        if (targetMember == null) {
            //目标成员不在该公会
            throw new CustomizeException(CustomizeErrorCode.NO_HAVE_MEMBER);
        }
        targetMember.setGuildPosition(positionId);
        GuildMemberEntity srcMember = playGuild.getGuildMemberMap().get(user.getUserId());

        if (srcMember.getGuildPosition().equals(GuildMemberType.President.getRoleId())
            && positionId != GuildMemberType.President.getRoleId()){
            // 会长
            guildTimer.modifyGuildMemberEntity(targetMember);
        }else if (srcMember.getGuildPosition().equals(GuildMemberType.VicePresident.getRoleId())
                && positionId != GuildMemberType.President.getRoleId()
                && positionId != GuildMemberType.VicePresident.getRoleId()){
            //副会长
            guildTimer.modifyGuildMemberEntity(targetMember);
        }else {
            // 权限不够
            throw new CustomizeException(CustomizeErrorCode.AUTH_NOT_ENOUGH);
        }


        if (targetMember.isOnline()){
            //在线
            GameMsg.AppointMemberResult appointMemberResult = GameMsg.AppointMemberResult.newBuilder()
                    .setPositionId(positionId)
                    .build();
            UserManager.getUserById(targetMember.getUserId()).getCtx().writeAndFlush(appointMemberResult);
        }
    }
}
