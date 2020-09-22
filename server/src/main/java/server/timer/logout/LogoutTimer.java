package server.timer.logout;

import entity.db.CurrUserStateEntity;
import entity.db.DbSendMailEntity;
import entity.db.DbTaskEntity;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import server.*;
import server.cmdhandler.task.listener.TaskUtil;
import server.cmdhandler.team.TeamUtil;
import server.model.PlayArena;
import server.model.User;
import server.service.MailService;
import server.service.TaskService;
import server.service.UserService;
import server.timer.mail.DbSendMailTimer;
import server.timer.state.DbUserStateTimer;
import util.CustomizeThreadFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public final class LogoutTimer {

    public LogoutTimer() {
    }

    @Autowired
    private MailService mailService;

    @Autowired
    private UserService userService;

    @Autowired
    TaskService taskService;

    @Autowired
    TaskUtil taskPublicMethod;

    private final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(
            1,
            new CustomizeThreadFactory("注销数据库;")
    );

    public void logout(User user) {

        if (user == null) {
            return;
        }

        // 队伍管理
        TeamUtil.getTeamUtil().quitTeam(user);

        user.setCurrDuplicate(null);

        ScheduledFuture<?> schedule = scheduledThreadPool.schedule(() -> {
            synchronized (user.getLOGOUT_MONITOR()) {
                if (user.getLogoutTimer() == null) {
                    return;
                }

                log.info("用户离线, userId = {}", user.getUserId());

                if (user.getPlayGuild() != null) {
                    user.getPlayGuild().getGuildMemberMap().get(user.getUserId()).setOnline(false);
                }
                CurrUserStateEntity userStateEntity = PublicMethod.getInstance().createUserState(user);

                // 保存用户所在地
                userService.modifyUserState(userStateEntity);
//            userStateTimer.modifyUserState(userStateEntity);

                //改变任务状态
                if (user.getPlayTask().isHaveTask()) {
                    DbTaskEntity taskEntity = taskPublicMethod.getTaskEntity(user);
                    taskService.modifyTaskState(taskEntity);
                }

                // 持久化,邮件
                mailService.modifyMailState(user.getMail().getMailEntityMap().values());
//            for (DbSendMailEntity mailEntity : user.getMail().getMailEntityMap().values()) {
//                mailTimer.modifyMailList(mailEntity);
//            }

                // 移除管道
                Broadcast.removeChannel(user.getCurSceneId(), user.getCtx().channel());

                // 移除用户
                UserManager.removeUser(user.getUserId());

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
            }


        }, 1, TimeUnit.MINUTES);


        user.setLogoutTimer(schedule);

    }

}
