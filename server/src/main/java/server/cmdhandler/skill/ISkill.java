package server.cmdhandler.skill;

import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;

/**
 * @author 张丰博
 */
public interface ISkill {

    /**
     * 技能处理方法
     * @param ctx
     * @param cmd
     */
    void skillHandle(ChannelHandlerContext ctx, GameMsg.UserSkillAttkCmd cmd);
}
