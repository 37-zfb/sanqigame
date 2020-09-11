package client.thread;

import client.model.Role;
import client.model.arena.PlayArenaClient;
import client.model.server.profession.Skill;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import client.model.PlayUserClient;
import msg.GameMsg;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 */
@Slf4j
public class ArenaThread {

    private static final ArenaThread ARENA_THREAD = new ArenaThread();


    private ArenaThread() {
    }

    /**
     * 自定义单线程的线程池,
     * 线程名称: MainThread
     */
    private final ExecutorService ex =
            new ThreadPoolExecutor(
                    1,
                    1,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    (newRunnable) -> {
                        Thread thread = new Thread(newRunnable);
                        thread.setName("ArenaThread");
                        return thread;
                    },
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );

    public static ArenaThread getInstance() {
        return ARENA_THREAD;
    }

    public void process(ChannelHandlerContext ctx, Role role) {

        if (ctx == null || role == null) {
            return;
        }
        ex.submit(() -> {
            try {
                log.info("当前线程 {}", Thread.currentThread().getName());

                sendCmd(ctx, role);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    private void sendCmd(ChannelHandlerContext ctx, Role role) {
        while (true) {

            PlayArenaClient playArenaClient = role.getARENA_CLIENT();


            log.info("请选择您的操作: ");
            System.out.println("======>1:选择对手;");
            if (playArenaClient.getChallengeUser() != null) {
                System.out.println("======>2:普通攻击;");
                System.out.println("======>3:技能;");
            }
            System.out.println("======>7:同意挑战;");
            System.out.println("======>8:拒接挑战;");
            System.out.println("======>9:退出竞技场;");

            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();

            if ("1".equals(command)) {
                Map<Integer, PlayUserClient> arenaUserMap = role.getARENA_CLIENT().getArenaUserMap();
                for (PlayUserClient arenaUser : arenaUserMap.values()) {
                    System.out.println(arenaUser.getUserId() + "、" + arenaUser.getUserName());
                }

                int userId = scanner.nextInt();
                GameMsg.UserChooseOpponentCmd chooseOpponentCmd = GameMsg.UserChooseOpponentCmd.newBuilder()
                        .setUserId(userId)
                        .build();
                ctx.writeAndFlush(chooseOpponentCmd);
                break;
            } else if ("2".equals(command)) {
                if (playArenaClient.getChallengeUser() != null){
                    PlayUserClient challengeUser = playArenaClient.getChallengeUser();

                    GameMsg.UserAttackCmd userAttackCmd = GameMsg.UserAttackCmd.newBuilder()
                            .setTargetUserId(challengeUser.getUserId())
                            .build();
                    ctx.writeAndFlush(userAttackCmd);
                }
            } else if ("3".equals(command)) {
                System.out.println("当前所拥有技能如下: ");
                for (Skill skill : role.getSkillMap().values()) {
                    System.out.println(skill.getId() + "、技能 名称: " + skill.getName() + " , \tcd: " + skill.getCdTime() + " , 是否冷却中: " + (skill.isCd() ? "冷却中;" : "未冷却;"));
                }
                int skillId = scanner.nextInt();

                GameMsg.UserSkillAttkCmd userSkillAttkCmd = GameMsg.UserSkillAttkCmd.newBuilder()
                        .setSkillId(skillId)
                        .build();
                ctx.writeAndFlush(userSkillAttkCmd);
            } else if ("4".equals(command)) {
                break;
            } else if ("5".equals(command)) {
                break;
            } else if ("6".equals(command)) {
                break;
            } else if ("7".equals(command)) {
                // 同意挑战
                GameMsg.TargetUserResponseCmd targetUserResponseCmd =
                        GameMsg.TargetUserResponseCmd.newBuilder()
                                .setIsAgree(true)
                                .setOriginateUserId(role.getARENA_CLIENT().getOriginateUserId())
                                .build();
                role.getARENA_CLIENT().setOriginateUserId(null);
                ctx.writeAndFlush(targetUserResponseCmd);
                break;
            } else if ("8".equals(command)) {
                // 拒绝挑战
                GameMsg.TargetUserResponseCmd targetUserResponseCmd =
                        GameMsg.TargetUserResponseCmd.newBuilder()
                                .setIsAgree(false)
                                .setOriginateUserId(role.getARENA_CLIENT().getOriginateUserId())
                                .build();
                role.getARENA_CLIENT().setOriginateUserId(null);
                ctx.writeAndFlush(targetUserResponseCmd);
                break;
            } else if ("9".equals(command)) {
                // 退出竞技场
                GameMsg.UserQuitArenaCmd userQuitArenaCmd = GameMsg.UserQuitArenaCmd.newBuilder().build();
                ctx.writeAndFlush(userQuitArenaCmd);
                break;
            }else {
                if (role.getCurrHp() == 0) {
                    System.out.println("已死亡,请退出副本;");
                }
                log.error("指令错误,请重新输入;");
            }
        }

    }

}
