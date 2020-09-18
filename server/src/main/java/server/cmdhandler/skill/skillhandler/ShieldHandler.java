package server.cmdhandler.skill.skillhandler;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.cmdhandler.skill.ISkill;

/**
 * @author 张丰博
 * 护盾
 */
@Component
@Slf4j
public class ShieldHandler implements ISkill{
    @Override
    public void skillHandle(ChannelHandlerContext ctx, GameMsg.UserSkillAttkCmd cmd) {

    }
}
