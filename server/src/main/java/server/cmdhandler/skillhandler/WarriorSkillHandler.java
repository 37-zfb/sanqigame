package server.cmdhandler.skillhandler;

import io.netty.channel.ChannelHandlerContext;
import model.profession.skill.WarriorSkillProperty;
import org.springframework.stereotype.Component;

/**
 * @author 张丰博
 */
@Component
public class WarriorSkillHandler implements ISkillHandler<WarriorSkillProperty> {

    @Override
    public void skillHandle(ChannelHandlerContext ctx, WarriorSkillProperty warriorSkillProperty) {
        System.out.println("战士技能================");
    }
}
