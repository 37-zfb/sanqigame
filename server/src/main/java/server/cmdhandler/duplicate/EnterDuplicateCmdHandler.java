package server.cmdhandler.duplicate;

import constant.DuplicateConst;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import model.duplicate.BossMonster;
import model.duplicate.BossSkill;
import model.duplicate.Duplicate;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import scene.GameData;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.UserManager;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 * <p>
 * 展示当前副本
 */
@Component
@Slf4j
public class EnterDuplicateCmdHandler implements ICmdHandler<GameMsg.EnterDuplicateCmd> {

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.EnterDuplicateCmd duplicateCmd) {

        MyUtil.checkIsNull(ctx, duplicateCmd);

        // 要进入的副本id
        int duplicateId = duplicateCmd.getDuplicateId();

        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        User user = UserManager.getUserById(userId);

        Duplicate duplicateTemplate = GameData.getInstance().getDuplicateMap().get(duplicateId);

        Duplicate duplicate = new Duplicate();
        duplicate.setId(duplicateId);
        duplicate.setName(duplicateTemplate.getName());
        duplicate.setStartTime(System.currentTimeMillis()+ DuplicateConst.INIT_TIME);

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
        user.setCurrDuplicate(duplicate);
        duplicate.setMinBoss();

        GameMsg.EnterDuplicateResult enterDuplicateResult =
                GameMsg.EnterDuplicateResult.newBuilder()
                        .setDuplicateId(duplicateId)
                        .setStartTime(duplicate.getStartTime())
                        .setBossMonsterId(duplicate.getCurrBossMonster().getId())
                        .build();
        ctx.channel().writeAndFlush(enterDuplicateResult);

    }

}
