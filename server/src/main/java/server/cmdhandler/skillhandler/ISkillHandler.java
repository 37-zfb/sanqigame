package server.cmdhandler.skillhandler;

import io.netty.channel.ChannelHandlerContext;
import model.profession.skill.AbstractSkillProperty;

/**
 * @author 张丰博
 */
public interface ISkillHandler<SkillCmd extends AbstractSkillProperty> {
    /**
     *  技能处理方法
     * @param ctx
     * @param skillCmd 技能类型
     * @param skillId  技能id
     */
     void skillHandle(ChannelHandlerContext ctx,SkillCmd skillCmd ,Integer skillId);
}
