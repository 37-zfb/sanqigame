package server.cmdhandler.guild;

import constant.EquipmentConst;
import entity.db.GuildMemberEntity;
import entity.db.UserEquipmentEntity;
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
import server.UserManager;
import server.service.UserService;
import util.MyUtil;

import java.util.List;

/**
 * @author 张丰博
 * 查看公会成员信息
 */
@Component
@Slf4j
public class LookGuildMemberInfoCmdHandler implements ICmdHandler<GameMsg.LookGuildMemberInfoCmd> {
    @Autowired
    private UserService userService;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.LookGuildMemberInfoCmd lookGuildMemberInfoCmd) {

        MyUtil.checkIsNull(ctx, lookGuildMemberInfoCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        if (user.getPlayGuild() == null) {
            //未加入公会
            throw new CustomizeException(CustomizeErrorCode.NOT_JOIN_GUILD);
        }

        int userId = lookGuildMemberInfoCmd.getUserId();

        PlayGuild playGuild = user.getPlayGuild();
        GuildMemberEntity guildMemberEntity = playGuild.getGuildMemberMap().get(userId);
        if (guildMemberEntity == null) {
            throw new CustomizeException(CustomizeErrorCode.NO_HAVE_MEMBER);
        }

        GameMsg.LookGuildMemberInfoResult.Builder newBuilder = GameMsg.LookGuildMemberInfoResult.newBuilder();

        User targetUser = UserManager.getUserById(userId);
        UserEquipmentEntity[] loadWearEqu = null;
        if (targetUser == null) {
            loadWearEqu = loadWearEqu(userId);
        } else {
            loadWearEqu = targetUser.getUserEquipmentArr();
        }

        for (UserEquipmentEntity equipmentEntity : loadWearEqu) {
            if (equipmentEntity != null) {
                newBuilder.addEquId(equipmentEntity.getPropsId());
            }
        }

        GameMsg.LookGuildMemberInfoResult lookGuildMemberInfoResult = newBuilder.build();
        ctx.writeAndFlush(lookGuildMemberInfoResult);
    }

    /**
     * 加载穿戴的装备
     *
     * @param userId 用户id
     */
    private UserEquipmentEntity[] loadWearEqu(Integer userId) {
        UserEquipmentEntity[] userEquipmentArr = new UserEquipmentEntity[9];
        List<UserEquipmentEntity> listEquipment = userService.listEquipmentWeared(userId, EquipmentConst.WEAR);
        for (int i = 0; i < listEquipment.size(); i++) {
            userEquipmentArr[i] = listEquipment.get(i);
        }
        return userEquipmentArr;
    }
}
