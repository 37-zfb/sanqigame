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
 * 转让会长
 */
@Component
@Slf4j
public class TransferPresidentCmdHandler implements ICmdHandler<GameMsg.TransferPresidentCmd> {

    @Autowired
    private DbGuildTimer guildTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.TransferPresidentCmd transferPresidentCmd) {

        MyUtil.checkIsNull(ctx, transferPresidentCmd);
        User user = PublicMethod.getInstance().getUser(ctx);


        PlayGuild playGuild = user.getPlayGuild();
        if (playGuild == null) {
            throw new CustomizeException(CustomizeErrorCode.NOT_JOIN_GUILD);
        }

        GuildMemberEntity srcEntity = playGuild.getGuildMemberMap().get(user.getUserId());
        if (!(srcEntity == null || srcEntity.getGuildPosition().equals(GuildMemberType.President.getRoleId()))) {
            //若不是会长
            throw new CustomizeException(CustomizeErrorCode.USER_NO_HAVE_GUILD_OR_NOT_PRESIDENT);
        }

        int targetUserId = transferPresidentCmd.getUserId();
        GuildMemberEntity targetEntity = playGuild.getGuildMemberMap().get(targetUserId);
        if (targetEntity == null) {
            //未加入此公会
            throw new CustomizeException(CustomizeErrorCode.NOT_JOIN_THIS_GUILD);
        }

        srcEntity.setGuildPosition(GuildMemberType.Member.getRoleId());
        targetEntity.setGuildPosition(GuildMemberType.President.getRoleId());

        guildTimer.modifyGuildMemberEntity(srcEntity);
        guildTimer.modifyGuildMemberEntity(targetEntity);

        GameMsg.TransferPresidentResult transferPresidentResult = GameMsg.TransferPresidentResult.newBuilder()
                .setUserId(targetUserId)
                .build();

        User targetUser = UserManager.getUserById(targetUserId);
        if (targetUser != null){
            //通知
            targetUser.getCtx().writeAndFlush(transferPresidentResult);
        }

        ctx.writeAndFlush(transferPresidentResult);
    }
}
