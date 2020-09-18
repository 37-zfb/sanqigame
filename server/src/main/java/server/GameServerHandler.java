package server;

import com.google.protobuf.GeneratedMessageV3;
import entity.db.DbSendMailEntity;
import entity.db.DbTaskEntity;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.context.ApplicationContext;
import entity.db.CurrUserStateEntity;
import server.cmdhandler.task.listener.TaskUtil;
import server.cmdhandler.team.TeamUtil;
import server.model.PlayArena;
import server.model.User;
import server.service.MailService;
import server.service.TaskService;
import server.timer.logout.LogoutTimer;
import server.timer.mail.DbSendMailTimer;
import server.timer.state.DbUserStateTimer;

/**
 * @author 张丰博
 */
@Slf4j
public class GameServerHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        // 客户端、服务端建立连接成功后，会执行此方法

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("消息类型: " + msg.getClass());

        if (msg instanceof GeneratedMessageV3) {
            MainThreadProcessor.getInstance().process(ctx, (GeneratedMessageV3) msg);
        }

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);

        User user = PublicMethod.getInstance().getUser(ctx);
        LogoutTimer logoutTimer = GameServer.APPLICATION_CONTEXT.getBean(LogoutTimer.class);
        logoutTimer.logout(user);

       /* // 拿到用户 Id
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        if (null == userId) {
            return;
        }

        log.info("用户离线, userId = {}", userId);

        ApplicationContext context = GameServer.APPLICATION_CONTEXT;
        TaskService taskService = context.getBean(TaskService.class);
        TaskUtil taskPublicMethod = context.getBean(TaskUtil.class);
        DbUserStateTimer userStateTimer = context.getBean(DbUserStateTimer.class);
        DbSendMailTimer mailTimer = context.getBean(DbSendMailTimer.class);

        User user = UserManager.getUserById(userId);
        if (user == null) {
            return;
        }

        user.setCurrDuplicate(null);

        if (user.getPlayGuild() != null) {
            user.getPlayGuild().getGuildMemberMap().get(userId).setOnline(false);
        }
        CurrUserStateEntity userStateEntity = PublicMethod.getInstance().createUserState(user);

        // 保存用户所在地
//        userService.modifyUserState(userStateEntity);
        userStateTimer.modifyUserState(userStateEntity);

        //改变任务状态
        if (user.getPlayTask().isHaveTask()) {
            DbTaskEntity taskEntity = taskPublicMethod.getTaskEntity(user);
            taskService.modifyTaskState(taskEntity);
        }

        // 持久化,邮件
//        mailService.modifyMailState(user.getMail().getMailEntityMap().values());
        for (DbSendMailEntity mailEntity : user.getMail().getMailEntityMap().values()) {
            mailTimer.modifyMailList(mailEntity);
        }

        // 队伍管理
        TeamUtil.getTeamUtil().quitTeam(user);

        // 移除管道
        Broadcast.removeChannel(user.getCurSceneId(), ctx.channel());

        // 移除用户
        UserManager.removeUser(userId);

        //取消定时器
        PublicMethod.getInstance().cancelMonsterAttack(user);


        PlayArena playArena = user.getPlayArena();
        if (playArena == null) {
            return;
        }
        ArenaManager.removeUser(user);

        if (playArena.getTargetUserId() == null) {
            return;
        }

        User targetUser = ArenaManager.getUserById(playArena.getTargetUserId());
        if (targetUser == null) {
            return;
        }
        targetUser.getPlayArena().setTargetUserId(null);
        GameMsg.UserDieResult userDieResult = GameMsg.UserDieResult.newBuilder()
                .setTargetUserId(user.getUserId())
                .build();
        targetUser.getCtx().writeAndFlush(userDieResult);
*/

//        // 广播用户离场的消息
//        GameMsg.UserQuitResult.Builder resultBuilder = GameMsg.UserQuitResult.newBuilder();
//        resultBuilder.setQuitUserId(userId);
//
//        GameMsg.UserQuitResult newResult = resultBuilder.build();
//        Broadcaster.broadcast(newResult);
//
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause.getMessage());
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
