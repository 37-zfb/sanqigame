package client.cmd;

import client.model.server.props.Equipment;
import client.model.server.props.Props;
import client.model.server.scene.Monster;
import client.model.server.scene.Scene;
import client.scene.GameData;
import client.thread.CmdThread;
import client.model.Role;
import client.model.SceneData;
import constant.SceneConst;
import entity.db.UserEquipmentEntity;
import io.netty.channel.ChannelHandlerContext;

import msg.GameMsg;
import type.EquipmentType;

import java.util.Map;

/**
 * @author 张丰博
 */
public class UserNormalAttkResultClient implements ICmd<GameMsg.AttkResult> {

    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.AttkResult attkResult) {

        if (ctx == null || attkResult == null) {
            return;
        }

        Role role = Role.getInstance();
        Scene curScene = SceneData.getInstance().getSceneMap().get(role.getCurrSceneId());
        //null
        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        UserEquipmentEntity[] userEquipmentEntityArr = role.getUserEquipmentEntityArr();
        if (attkResult.getSubHp() == -1) {
            // 此时，当前场景怪被全部击杀 ,减耐久度
            for (int i = 0; i < userEquipmentEntityArr.length; i++) {
                if (userEquipmentEntityArr[i] != null && ((Equipment)propsMap.get(userEquipmentEntityArr[i].getPropsId()).getPropsProperty()).getEquipmentType()==EquipmentType.Weapon) {
                    userEquipmentEntityArr[i].setDurability(userEquipmentEntityArr[i].getDurability()-1);
                }

            }

            System.out.println(curScene.getName() + " 怪全部被击杀!");

        } else if (attkResult.getSubHp() == SceneConst.NO_MONSTER) {
            // 当前场景没怪,不减耐久度
            System.out.println(curScene.getName() + " 没有怪!");

        } else {
            for (int i = 0; i < userEquipmentEntityArr.length; i++) {
                if (userEquipmentEntityArr[i] != null && ((Equipment)propsMap.get(userEquipmentEntityArr[i].getPropsId()).getPropsProperty()).getEquipmentType()==EquipmentType.Weapon) {
                    userEquipmentEntityArr[i].setDurability(userEquipmentEntityArr[i].getDurability()-1);
                }
            }
            // 正常攻击
            int subHp = attkResult.getSubHp();
            Integer monsterId = attkResult.getMonsterId();
            // 被攻击id怪
            for (Monster monster : curScene.getMonsterMap().values()) {
                if (monster.getId().equals(monsterId)) {
                    // 减血
                    monster.setHp(monster.getHp() - subHp);
                    System.out.println(monster.getName() + " hp: -" + subHp + ", 剩余hp: " + monster.getHp());
                }
            }

        }

        CmdThread.getInstance().process(ctx, role, curScene.getNpcMap().values());

    }

}
