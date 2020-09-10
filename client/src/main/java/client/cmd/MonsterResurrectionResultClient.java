package client.cmd;

import client.model.Role;
import client.model.SceneData;
import client.model.server.scene.Monster;
import client.model.server.scene.Scene;
import constant.MonsterConst;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 */
public class MonsterResurrectionResultClient implements ICmd<GameMsg.MonsterResurrectionResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.MonsterResurrectionResult monsterResurrectionResult) {

        MyUtil.checkIsNull(ctx, monsterResurrectionResult);
        Role role = Role.getInstance();

        int sceneId = monsterResurrectionResult.getSceneId();

        Scene scene = SceneData.getInstance().getSceneMap().get(sceneId);

        System.out.println(scene.getName() + " 的怪复活;");

        Map<Integer, Monster> monsterMap = scene.getMonsterMap();
        for (Monster monster : monsterMap.values()) {
            monster.setHp(MonsterConst.HP);
        }

    }
}
