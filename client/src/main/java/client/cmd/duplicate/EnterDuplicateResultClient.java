package client.cmd.duplicate;

import client.CmdThread;
import client.model.SceneData;
import client.model.server.duplicate.BossMonster;
import client.model.server.duplicate.BossSkill;
import client.model.server.duplicate.Duplicate;
import client.scene.GameData;
import client.module.BossModule;
import client.cmd.ICmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;

import msg.GameMsg;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 */
public class EnterDuplicateResultClient implements ICmd<GameMsg.EnterDuplicateResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.EnterDuplicateResult enterDuplicateResult) {

        MyUtil.checkIsNull(ctx, enterDuplicateResult);
        Role role = Role.getInstance();


        int duplicateId = enterDuplicateResult.getDuplicateId();
        Duplicate duplicateTemplate = GameData.getInstance().getDuplicateMap().get(duplicateId);

        Duplicate duplicate = new Duplicate();
        duplicate.setId(duplicateId);
        duplicate.setName(duplicateTemplate.getName());
        duplicate.setStartTime(enterDuplicateResult.getStartTime());

        Map<Integer, BossMonster> bossMonsterMap = duplicate.getBossMonsterMap();
        for (Map.Entry<Integer, BossMonster> bossMonsterEntry : duplicateTemplate.getBossMonsterMap().entrySet()) {
            BossMonster value = bossMonsterEntry.getValue();

            BossMonster bossMonster = new BossMonster();
            bossMonster.setId(value.getId());
            bossMonster.setBossName(value.getBossName());
            bossMonster.setDuplicateId(value.getDuplicateId());
            bossMonster.setBaseDamage(value.getBaseDamage());
            bossMonster.setHp(value.getHp());
            Map<Integer, BossSkill> bossSkillMap = bossMonster.getBossSkillMap();

            for (Map.Entry<Integer, BossSkill> skillEntry : value.getBossSkillMap().entrySet()) {
                bossSkillMap.put(skillEntry.getKey(), skillEntry.getValue());
            }
            bossMonsterMap.put(bossMonsterEntry.getKey(), bossMonster);
        }

        // 设置当前副本
        role.setCurrDuplicate(duplicate);
        // 设置当前怪
        duplicate.setMinBoss();
        //此时，已进入副本；进入另一个线程？？？
        if (role.getId() == enterDuplicateResult.getUserId()) {
//            BossModule.getInstance().process(ctx, role);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());

        } else {
            System.out.println("队伍进入 " + duplicate.getName());
        }
    }

}

