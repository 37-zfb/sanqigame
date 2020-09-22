package server.cmdhandler.skill;

import constant.SkillConst;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import server.GameServer;
import server.model.PlayTeam;
import server.model.User;
import server.model.duplicate.Duplicate;
import server.model.profession.Skill;
import server.model.profession.skill.AbstractSkillProperty;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.scene.GameData;
import server.PublicMethod;
import server.cmdhandler.CmdHandlerFactory;
import server.cmdhandler.ICmdHandler;
import server.model.scene.Scene;
import server.service.UserService;
import type.SkillType;
import util.MyUtil;

/**
 * 技能
 *
 * @author 张丰博
 */
@Slf4j
@Component
public class UserSkillAttkCmdHandler implements ICmdHandler<GameMsg.UserSkillAttkCmd> {

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserSkillAttkCmd cmd) {

        MyUtil.checkIsNull(ctx, cmd);

        User user = PublicMethod.getInstance().getUser(ctx);
        Skill skill = user.getSkillMap().get(cmd.getSkillId());
        if (skill == null) {
            //该职业不存在此技能;
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_HAVE_THIS_SKILL);
        }

        Integer skillId = skill.getId();
        String clazzName = SkillType.getClazzNameById(skillId);
        if (clazzName == null) {
            return;
        }

        if (user.getCurrHp() <= 0) {
            return;
        }

        //判断用户此时状态
        SkillUtil.getSkillUtil().skillDestination(user);

        // 计算当前mp
        user.calCurrMp();
        user.resumeMpTime();

        if ((System.currentTimeMillis() - skill.getLastUseTime()) < skill.getCdTime() * SkillConst.CD_UNIt_SWITCH) {
            // 此时技能未冷却好
            log.info("用户 {} 技能 {} 冷却中;", user.getUserName(), skill.getName());
            throw new CustomizeException(CustomizeErrorCode.SKILL_CD);
        } else if (user.getCurrMp() < skill.getConsumeMp()) {
            //此时MP不够
            log.info("用户 {} 当前MP {} ,需要 MP {} ,MP不足!", user.getUserName(), user.getCurrMp(), skill.getConsumeMp());
            throw new CustomizeException(CustomizeErrorCode.MP_NOT_ENOUGH);
        }

        skill.setLastUseTime(System.currentTimeMillis());

        try {
            ISkill iSkill = (ISkill) GameServer.APPLICATION_CONTEXT.getBean(Class.forName(clazzName));
            iSkill.skillHandle(ctx, cmd);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }


    }

}
