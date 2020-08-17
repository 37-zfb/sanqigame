package server.cmdhandler.mail;

import entity.db.DbSendMailEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.service.MailService;
import util.MyUtil;

import java.util.List;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public class UserSeeMailCmdHandler implements ICmdHandler<GameMsg.UserSeeMailCmd> {
    @Autowired
    private MailService mailService;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserSeeMailCmd userSeeMailCmd) {

        MyUtil.checkIsNull(ctx,userSeeMailCmd);
        User user = PublicMethod.getInstance().getUser(ctx);





    }
}
