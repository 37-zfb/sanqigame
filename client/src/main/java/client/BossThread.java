package client;

import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import model.duplicate.BossMonster;
import model.duplicate.Duplicate;
import model.profession.Skill;
import msg.GameMsg;
import scene.GameData;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 */
@Slf4j
public class BossThread {

    private static final BossThread BOSS_THREAD = new BossThread();

    private BossThread() {
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
                        thread.setName("BossThread");
                        return thread;
                    },
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );

    public static BossThread getInstance() {
        return BOSS_THREAD;
    }

    public void process(ChannelHandlerContext ctx, Role role) {

        if (ctx == null || role == null) {
            return;
        }

        ex.submit(() -> {
            try {

                Duplicate currDuplicate = role.getCurrDuplicate();

                Map<Integer, BossMonster> monsterMap = GameData.getInstance().getDuplicateMap().get(currDuplicate.getId()).getBossMonsterMap();

                Map<Integer, BossMonster> bossMonsterMap = currDuplicate.getBossMonsterMap();
                long currTime = currDuplicate.getStartTime() - System.currentTimeMillis();
                System.out.println("副本: " + role.getCurrDuplicate().getName() + " Boss数量: " + monsterMap.size());

                // 当前的boss
                BossMonster currBossMonster = role.getCurrDuplicate().getCurrBossMonster();
                System.out.println("第 " + (monsterMap.size() - bossMonsterMap.size()) + " 个 Boss");
                System.out.println("boss: " + currBossMonster.getBossName() + " hp: " + currBossMonster.getHp());
                // 准备时间
                if (role.isEnter()) {
                    System.out.println(Math.ceil(currTime / 1000.0) + " 秒后,战斗开始;");
                    Thread.sleep((long) (Math.ceil(currTime / 1000.0) * 1000));
                }


                sendCmd(ctx,role);

//                //怪已死，移除集合
//                bossMonsterMap.remove(bossMonsterEntry.get().getKey());
//                // 进入下一个怪或者此副本结束
//                role.setEnter(true);


            } catch (Exception e) {
                e.printStackTrace();
            }

        });

    }


    private void sendCmd(ChannelHandlerContext ctx,Role role) {
        // 判断 role 对象中是否有 Duplicate对象 来区分，去哪个线程
        while (true) {

            log.info("请选择您的操作: ");
            System.out.println("======>1:普通攻击;");
            System.out.println("======>2:技能列表;");
            System.out.println("======>3:使用MP/HP药剂;");
            System.out.println("======>4:背包;");
            System.out.println("======>5:装备栏;");
            System.out.println("======>6:穿戴装备;");
            System.out.println("======>7:卸下装备;");
            System.out.println("======>8:修理装备;");

            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();

            if ("1".equals(command)) {
                GameMsg.AttkBossCmd attkBossCmd = GameMsg.AttkBossCmd.newBuilder().build();
                // 发送数据
                ctx.writeAndFlush(attkBossCmd);
                break;
            } else if ("2".equals(command)) {

                System.out.println("当前所拥有技能如下: ");
                for (Skill skill : role.getSkillMap().values()) {
                    System.out.println(skill.getId() + "、技能 名称: " + skill.getName() + " , \tcd: " + skill.getCdTime() + " , 是否冷却中: " + (skill.isCd() ? "冷却中;" : "未冷却;"));
                }
                int skillId = scanner.nextInt();

                GameMsg.UserSkillAttkCmd userSkillAttkCmd = GameMsg.UserSkillAttkCmd.newBuilder()
                        .setSkillId(skillId)
                        .build();
                ctx.writeAndFlush(userSkillAttkCmd);
                break;

            } else if ("3".equals(command)) {

            } else if ("4".equals(command)) {

            } else if ("5".equals(command)) {

            } else if ("6".equals(command)) {

            } else if ("7".equals(command)) {

            } else if ("8".equals(command)) {

            } else {
                log.error("指令错误,请重新输入;");
            }
        }

    }


}
