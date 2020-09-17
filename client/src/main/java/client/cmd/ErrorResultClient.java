package client.cmd;

import client.GameClient;
import client.model.Role;
import client.model.SceneData;
import client.CmdThread;
import constant.ProfessionConst;
import exception.CustomizeErrorCode;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class ErrorResultClient implements ICmd<GameMsg.ErrorResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.ErrorResult dealErrorResult) {
        MyUtil.checkIsNull(ctx, dealErrorResult);
        Role role = Role.getInstance();

        int code = dealErrorResult.getCode();
        String msg = dealErrorResult.getMsg();
        if (code == CustomizeErrorCode.DEAL_REQUEST_ERROR.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
        } else if (code == CustomizeErrorCode.USER_NOT_DEAL_STATUS.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        } else if (code == CustomizeErrorCode.ORIGINATE_USER_NOT_FOUNT.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        } else if (code == CustomizeErrorCode.ORIGINATE_USER_NOT_REQUEST.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        } else if (code == CustomizeErrorCode.USER_MONEY_NOT_ENOUGH.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        } else if (code == CustomizeErrorCode.USER_HAVE_GUILD.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        } else if (code == CustomizeErrorCode.GUILD_ALREADY_EXIST.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        } else if (code == CustomizeErrorCode.USER_NO_HAVE_GUILD_OR_NOT_PRESIDENT.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        } else if (code == CustomizeErrorCode.GUILD_NOT_EXIST.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        } else if (code == CustomizeErrorCode.GUILD_REACH_LIMIT.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        } else if (code == -99999) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
//            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        } else if (code == CustomizeErrorCode.USER_MONEY_INSUFFICIENT.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
        } else if (code == CustomizeErrorCode.USER_ALREADY_LOGIN.getCode() || code == CustomizeErrorCode.USER_NOT_FOUND.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
            GameClient.cmdLogin(ctx.channel());
        } else if (CustomizeErrorCode.USER_EXISTS.getCode() == code) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
            GameClient.cmdLogin(ctx.channel());
        } else if (code == CustomizeErrorCode.USER_NOT_HAVE_THIS_SKILL.getCode()
                || code == CustomizeErrorCode.SCENE_NOT_MONSTER.getCode()
                || code == CustomizeErrorCode.SKILL_CD.getCode()
                || code == CustomizeErrorCode.MP_NOT_ENOUGH.getCode()) {
            //技能
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        } else if (code == CustomizeErrorCode.POTION_CD_TIME.getCode()) {

        } else if (code == CustomizeErrorCode.NOT_TEAM_LEADER.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        } else if (code == CustomizeErrorCode.DUPLICATE_TIME_OUT.getCode()) {

            role.setCurrDuplicate(null);
            role.setCurrHp(ProfessionConst.HP);
            role.setCurrMp(ProfessionConst.MP);
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        } else if (code == CustomizeErrorCode.ALL_MONSTER_DIE.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
//            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        } else if (code == CustomizeErrorCode.MAIL_NUMBER_OVERFLOW.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
        } else if (code == CustomizeErrorCode.TARGET_NOT_EXIST.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
        } else if (code == CustomizeErrorCode.TARGET_NOT_COMPLETE.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
        } else if (code == CustomizeErrorCode.TASK_NOT_FOUND.getCode()) {
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
            CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
        }else if (code == CustomizeErrorCode.USER_DIE.getCode()){
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
        }else if (code == CustomizeErrorCode.DEAL_STATE.getCode()){
            System.out.println("错误代号: " + code + " 错误信息: " + msg);
        }


    }
}
