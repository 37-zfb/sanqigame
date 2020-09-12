package server.cmdhandler.guild;

import entity.db.CurrUserStateEntity;
import entity.db.DbGuildEquipment;
import entity.db.DbGuildPotion;
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
import server.model.UserManager;
import server.model.props.Props;
import server.timer.guild.DbGuildTimer;
import server.timer.state.DbUserStateTimer;
import type.GuildMemberType;
import type.PropsType;
import util.MyUtil;

import java.util.Map;

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


        PlayGuild playGuild = user.getPlayGuild();
        guildTimer.deleteGuildEntity(playGuild.getGuildEntity());
        guildTimer.deleteGuildMemberEntity(playGuild.getGuildMemberMap());

        Map<Integer, Props> warehouseProps = playGuild.getWAREHOUSE_PROPS();
        for (Map.Entry<Integer, Props> entry : warehouseProps.entrySet()) {
            if (entry.getValue().getPropsProperty().getType() == PropsType.Equipment) {
                DbGuildEquipment guildEquipment = new DbGuildEquipment();
                guildEquipment.setGuildId(playGuild.getId());
                guildEquipment.setLocation(entry.getKey());
                guildTimer.deleteGuildEquipment(guildEquipment);
            }

            if (entry.getValue().getPropsProperty().getType() == PropsType.Potion) {
                DbGuildPotion guildPotion = new DbGuildPotion();
                guildPotion.setLocation(entry.getKey());
                guildPotion.setGuildId(playGuild.getId());
                guildTimer.deleteGuildPotion(guildPotion);

            }
        }


        GuildManager.removeGuild(playGuild);
        log.info("用户: {} 解散 公会: {}", user.getUserName(), playGuild.getGuildEntity().getGuildName());


        GameMsg.UserDissolveGuildResult.Builder newBuilder = GameMsg.UserDissolveGuildResult.newBuilder();

        for (GuildMemberEntity guildMemberEntity : playGuild.getGuildMemberMap().values()) {
            CurrUserStateEntity userState = PublicMethod.getInstance().createUserState(UserManager.getUserById(guildMemberEntity.getUserId()));
            userState.setGuildId(GuildMemberType.Public.getRoleId());
            userStateTimer.modifyUserState(userState);
            if (guildMemberEntity.isOnline()) {
                User guildMember = UserManager.getUserById(guildMemberEntity.getUserId());
                if (guildMember == null) {
                    continue;
                }

                GameMsg.UserDissolveGuildResult build = newBuilder
                        .setUserId(user.getUserId())
                        .build();
                guildMember.setPlayGuild(null);
                guildMember.getCtx().writeAndFlush(build);
            }
        }

    }


}
