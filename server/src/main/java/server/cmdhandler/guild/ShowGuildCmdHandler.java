package server.cmdhandler.guild;

import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.GuildManager;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.PlayGuild;
import server.model.User;
import util.MyUtil;

import java.util.Collection;

/**
 * @author 张丰博
 * 展示公会
 */
@Component
@Slf4j
public class ShowGuildCmdHandler implements ICmdHandler<GameMsg.ShowGuildCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.ShowGuildCmd showGuildCmd) {
        MyUtil.checkIsNull(ctx, showGuildCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        if (user.getPlayGuild() != null) {
            //未加入公会
            throw new CustomizeException(CustomizeErrorCode.USER_HAVE_GUILD);
        }

        GameMsg.ShowGuildResult.Builder newBuilder = GameMsg.ShowGuildResult.newBuilder();

        Collection<PlayGuild> playGuildCollection = GuildManager.listPlayGuild();
        for (PlayGuild playGuild : playGuildCollection) {
            GameMsg.Guild.Builder guildInfo = GameMsg.Guild.newBuilder()
                    .setGuildId(playGuild.getGuildEntity().getId())
                    .setGuildName(playGuild.getGuildEntity().getGuildName());
            newBuilder.addGuild(guildInfo);
        }
        log.info("用户 {} 查看所有公会;", user.getUserName());
        GameMsg.ShowGuildResult showGuildResult = newBuilder.build();
        ctx.writeAndFlush(showGuildResult);
    }
}
