package server.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import entity.db.UserEquipmentEntity;
import server.model.User;
import server.model.UserManager;
import model.props.Potion;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public class BackpackCmdHandler implements ICmdHandler<GameMsg.BackpackCmd> {

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.BackpackCmd cmd) {
        if (ctx == null || cmd == null) {
            return;
        }

        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();

        User user = UserManager.getUserById(userId);
        log.info("展示用户 {} 背包;",user.getUserName());

    }
}
