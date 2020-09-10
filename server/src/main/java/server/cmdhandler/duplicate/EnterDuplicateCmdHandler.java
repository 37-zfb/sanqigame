package server.cmdhandler.duplicate;

import constant.DuplicateConst;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import server.model.duplicate.BossMonster;
import server.model.duplicate.BossSkill;
import server.model.duplicate.Duplicate;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.scene.GameData;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.UserManager;
import util.MyUtil;

import java.util.Map;

/**
 * @author 张丰博
 * 进入副本
 */
@Component
@Slf4j
public class EnterDuplicateCmdHandler implements ICmdHandler<GameMsg.EnterDuplicateCmd> {

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.EnterDuplicateCmd duplicateCmd) {

        MyUtil.checkIsNull(ctx, duplicateCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        // 要进入的副本id
        int duplicateId = duplicateCmd.getDuplicateId();

        if (user.getPlayTeam() != null && !user.getPlayTeam().getTeamLeaderId().equals(user.getUserId())) {
            //当前用户是不是此队伍的 队长
            throw new CustomizeException(CustomizeErrorCode.NOT_TEAM_LEADER);
        }

        Duplicate duplicateTemplate = GameData.getInstance().getDuplicateMap().get(duplicateId);

        Duplicate duplicate = new Duplicate();
        duplicate.setId(duplicateId);
        duplicate.setName(duplicateTemplate.getName());
        duplicate.setStartTime(System.currentTimeMillis() + DuplicateConst.INIT_TIME);

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

        duplicate.setMinBoss();
        // 设置当前副本
        if (user.getPlayTeam() != null) {
            //设置 队伍副本；此时要通知队伍成员
            user.getPlayTeam().setCurrDuplicate(duplicate);
            noticeUser(duplicate, user.getUserId(), user.getPlayTeam().getTEAM_MEMBER());
            log.info("队长 {}，带队进入副本 {};", user.getUserName(), duplicate.getName());
        } else {
            user.setCurrDuplicate(duplicate);
            noticeUser(duplicate, user.getUserId(), user.getUserId());
            log.info("{} ，进入副本 {};", user.getUserName(), duplicate.getName());
        }

    }

    /**
     * @param duplicate
     * @param teamLeaderId
     * @param ids
     */
    private void noticeUser(Duplicate duplicate, Integer teamLeaderId, Integer... ids) {
        for (Integer id : ids) {
            if (id == null) {
                continue;
            }

            User user = UserManager.getUserById(id);
            if (user == null){
                continue;
            }

            GameMsg.EnterDuplicateResult enterDuplicateResult =
                    GameMsg.EnterDuplicateResult.newBuilder()
                            .setDuplicateId(duplicate.getId())
                            .setUserId(teamLeaderId)
                            .setStartTime(duplicate.getStartTime())
                            .setBossMonsterId(duplicate.getCurrBossMonster().getId())
                            .build();
            user.getCtx().channel().writeAndFlush(enterDuplicateResult);
        }
    }

}
