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
 * 修改职位
 */
@Component
@Slf4j
public class ModifyGuildPositionCmdHandler implements ICmdHandler<GameMsg.ModifyGuildPositionCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.ModifyGuildPositionCmd modifyGuildPositionCmd) {

        MyUtil.checkIsNull(ctx,modifyGuildPositionCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayGuild playGuild = user.getPlayGuild();
        if (playGuild == null) {
            //未加入公会
            throw new CustomizeException(CustomizeErrorCode.NOT_JOIN_GUILD);
        }

        int positionId = modifyGuildPositionCmd.getPositionId();
        GuildMemberEntity memberEntity = playGuild.getGuildMemberMap().get(user.getUserId());

        memberEntity.setGuildPosition(positionId);
    }
}
