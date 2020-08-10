package client.cmd;

import client.CmdThread;
import client.model.Role;
import client.model.SceneData;
import io.netty.channel.ChannelHandlerContext;
import model.scene.Monster;
import model.scene.Npc;
import model.scene.Scene;
import msg.GameMsg;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 */
public class UserSwitchSceneCmdClient implements ICmd<GameMsg.UserSwitchSceneResult> {

    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserSwitchSceneResult msg) {
        if (ctx == null || msg == null) {
            return;
        }

        Role role = Role.getInstance();
        // 场景之间移动


        // 清空正在攻击自己的怪
//        SceneData.getInstance().getMonsterAttking().clear();

        // 1、获取当前场景中的npc

        List<GameMsg.UserSwitchSceneResult.Npc> npcList = msg.getNpcList();
        Scene scene = SceneData.getInstance().getSceneMap().get(role.getCurrSceneId());
        Map<Integer, Npc> npcMap = scene.getNpcMap();
        npcMap.clear();
        for (GameMsg.UserSwitchSceneResult.Npc npc : npcList) {
            npcMap.put(npc.getId(),new Npc(npc.getId(),npc.getName(), npc.getSceneId(), npc.getInfo()));
        }

        // 2、获取当前场景中的 怪
        List<GameMsg.UserSwitchSceneResult.MonsterInfo> monsterInfoList = msg.getMonsterInfoList();
        Map<Integer, Monster> monsterMap = scene.getMonsterMap();
        monsterMap.clear();
        for (GameMsg.UserSwitchSceneResult.MonsterInfo monsterInfo : monsterInfoList) {
            Monster monster = new Monster();
            monster.setName(monsterInfo.getName());
            monster.setHp(monsterInfo.getHp());
            monster.setId(monsterInfo.getMonsterId());
            monsterMap.put(monsterInfo.getMonsterId(),monster);
        }


        CmdThread.getInstance().process(ctx,role,SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());

    }

}
