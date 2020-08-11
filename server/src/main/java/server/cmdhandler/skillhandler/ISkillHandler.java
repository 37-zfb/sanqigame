package server.cmdhandler.skillhandler;

import io.netty.channel.ChannelHandlerContext;
import model.profession.skill.AbstractSkillProperty;

/**
 * @author 张丰博
 */
public interface ISkillHandler<SkillCmd extends AbstractSkillProperty> {
    /**
     *  处理技能请求
     * @param ctx
     * @param skillCmd 具体技能类
     */
     void skillHandle(ChannelHandlerContext ctx,SkillCmd skillCmd);
}
