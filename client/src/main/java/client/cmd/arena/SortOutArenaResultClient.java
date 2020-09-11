package client.cmd.arena;

import client.cmd.ICmd;
import client.model.Role;
import client.model.server.profession.Profession;
import constant.ProfessionConst;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class SortOutArenaResultClient implements ICmd<GameMsg.SortOutArenaResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.SortOutArenaResult sortOutArenaResult) {

        MyUtil.checkIsNull(ctx, sortOutArenaResult);
        Role role = Role.getInstance();
        role.setCurrMp(ProfessionConst.MP);
        role.setCurrHp(ProfessionConst.HP);

    }
}
