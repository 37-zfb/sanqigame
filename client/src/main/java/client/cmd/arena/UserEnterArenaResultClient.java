package client.cmd.arena;

import client.cmd.ICmd;
import client.model.Role;
import client.thread.ArenaThread;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import client.model.PlayUserClient;
import msg.GameMsg;
import util.MyUtil;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 */
@Slf4j
public class UserEnterArenaResultClient implements ICmd<GameMsg.UserEnterArenaResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserEnterArenaResult userEnterArenaResult) {
        MyUtil.checkIsNull(ctx, userEnterArenaResult);
        Role role = Role.getInstance();

        Map<Integer, PlayUserClient> arenaUserMap = role.getARENA_CLIENT().getArenaUserMap();

        List<GameMsg.UserInfo> userInfoList = userEnterArenaResult.getUserInfoList();
        for (GameMsg.UserInfo userInfo : userInfoList) {
            arenaUserMap.put(userInfo.getUserId(), new PlayUserClient(userInfo.getUserId(), userInfo.getUserName()));
        }
        if (!role.getARENA_CLIENT().isInArena()) {
            ArenaThread.getInstance().process(ctx, role);
            // 标识已进入竞技场
            role.getARENA_CLIENT().setInArena(true);
        }
    }
}