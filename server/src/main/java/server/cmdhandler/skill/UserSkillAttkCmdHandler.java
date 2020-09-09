package server.cmdhandler.skill;

import constant.SkillConst;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
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

/**
 * 技能攻击怪
 *
 * @author 张丰博
 */
@Slf4j
@Component
public class UserSkillAttkCmdHandler implements ICmdHandler<GameMsg.UserSkillAttkCmd> {
    @Autowired
    private UserService userService;


    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserSkillAttkCmd cmd) {

        if (ctx == null || cmd == null) {
            return;
        }

        User user = PublicMethod.getInstance().getUser(ctx);
        Skill skill = user.getSkillMap().get(cmd.getSkillId());
        if (skill == null){
            //该职业不存在此技能;
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_HAVE_THIS_SKILL);

        }

        // 计算当前mp
        // 计算当前mp值
        user.calCurrMp();
        user.resumeMpTime();

        Duplicate currDuplicate = null;
        //先判断是否有副本
        PlayTeam playTeam = user.getPlayTeam();
        if (playTeam == null) {
            currDuplicate = user.getCurrDuplicate();
        } else {
            currDuplicate = playTeam.getCurrDuplicate();
        }
        //所在场景
        Scene scene = GameData.getInstance().getSceneMap().get(user.getCurSceneId());
        // 判断当前场景中是否有怪
        if (currDuplicate == null && scene.getMonsterMap().size() == 0) {
            // 当前场景没有怪
            log.info("场景: {} 没有怪;", scene.getName());
            throw new CustomizeException(CustomizeErrorCode.SCENE_NOT_MONSTER);
        }

        if ((System.currentTimeMillis() - skill.getLastUseTime()) < skill.getCdTime() * SkillConst.CD_UNIt_SWITCH) {
            // 此时技能未冷却好
            log.info("用户 {} 技能 {} 冷却中;", user.getUserName(), skill.getName());
            throw new CustomizeException(CustomizeErrorCode.SKILL_CD);
        } else if (user.getCurrMp() < skill.getConsumeMp()) {
            //此时MP不够
            log.info("用户 {} 当前MP {} ,需要 MP {} ,MP不足!", user.getUserName(), user.getCurrMp(), skill.getConsumeMp());
            throw new CustomizeException(CustomizeErrorCode.MP_NOT_ENOUGH);
        }


        ISkillHandler<? extends AbstractSkillProperty> skillHandlerByClazz = CmdHandlerFactory.getSkillHandlerByClazz(skill.getSkillProperty().getClass());
        skillHandlerByClazz.skillHandle(ctx, cast(skill.getSkillProperty()),cmd.getSkillId());

    }


    public <SkillCmd extends AbstractSkillProperty> SkillCmd cast(AbstractSkillProperty skillProperty) {
        if (skillProperty == null) {
            return null;
        }

        return (SkillCmd) skillProperty;
    }




}
