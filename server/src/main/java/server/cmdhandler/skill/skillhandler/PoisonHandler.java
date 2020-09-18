package server.cmdhandler.skill.skillhandler;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.cmdhandler.skill.ISkill;

/**
 * @author 张丰博
 * 放毒
 */
@Component
@Slf4j
public class PoisonHandler implements ISkill {
    @Override
    public void skillHandle(ChannelHandlerContext ctx, GameMsg.UserSkillAttkCmd cmd) {


    }
}
