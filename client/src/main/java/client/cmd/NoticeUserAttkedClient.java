package client.cmd;

import client.thread.CmdThread;
import client.model.Role;
import client.model.SceneData;
import io.netty.channel.ChannelHandlerContext;
import model.scene.Monster;
import model.scene.Scene;
import msg.GameMsg;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 */
public class NoticeUserAttkedClient implements ICmd<GameMsg.NoticeUserAttked> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.NoticeUserAttked result) {

        if (ctx == null || result == null) {
            return;
        }

        Role role = Role.getInstance();
        Scene scene = SceneData.getInstance().getSceneMap().get(role.getCurrSceneId());
        Map<Integer, Monster> monsterMap = scene.getMonsterMap();
        List<Integer> monsterIdList = result.getMonsterIdList();
        if (monsterIdList.size() != 0) {
            for (Integer id : monsterIdList) {
                for (Monster monster : monsterMap.values()) {
                    if (monster.getId().equals(id)){
                        System.out.println("受到: " + monster.getName() + " 的攻击!");
                    }
                }
            }
        }
        CmdThread.getInstance().process(ctx, Role.getInstance(), scene.getNpcMap().values());
    }
}
