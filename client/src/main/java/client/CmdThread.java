package client;

import client.cmd.UserCmd;
import client.model.Role;
import io.netty.channel.ChannelHandlerContext;
import model.scene.Npc;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 */
public class CmdThread {

    private static final CmdThread CMD_THREAD = new CmdThread();

    private CmdThread() {
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
                        thread.setName("CmdThread");
                        return thread;
                    },
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );

    public static CmdThread getInstance() {
        return CMD_THREAD;
    }

    public void process(ChannelHandlerContext ctx, Role role, Collection<Npc> npcList) {

        if (ctx == null || role == null || npcList == null){
            return;
        }

        ex.submit(() -> {
            try {
                Thread.sleep(200);

                Object cmd = UserCmd.operation(role, npcList,ctx);
                UserCmd.sendCmd(ctx,  cmd);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });

    }
}
