package server.cmdhandler.guild;

import entity.db.GuildMemberEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayGuild;
import server.model.User;
import util.MyUtil;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public class ShowGuildMemberCmdHandler implements ICmdHandler<GameMsg.ShowGuildMemberCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.ShowGuildMemberCmd showGuildMemberCmd) {
        MyUtil.checkIsNull(ctx, showGuildMemberCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        if (user.getPlayGuild() == null) {
            //未加入公会
            throw new CustomizeException(CustomizeErrorCode.NOT_JOIN_GUILD);
        }

        GameMsg.ShowGuildMemberResult.Builder newBuilder = GameMsg.ShowGuildMemberResult.newBuilder();

        PlayGuild playGuild = user.getPlayGuild();
        for (GuildMemberEntity memberEntity : playGuild.getGuildMemberMap().values()) {
            GameMsg.UserInfo.Builder userInfo = GameMsg.UserInfo.newBuilder()
                    .setUserId(memberEntity.getUserId())
                    .setUserName(memberEntity.getUserName())
                    .setIsOnline(memberEntity.isOnline());
            newBuilder.addUserInfo(userInfo);
        }

        log.info("用户 {} 查看公会成员;", user.getUserName());
        GameMsg.ShowGuildMemberResult showGuildMemberResult = newBuilder.build();
        ctx.writeAndFlush(showGuildMemberResult);
    }
}
