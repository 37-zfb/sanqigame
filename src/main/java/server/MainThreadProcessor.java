package server;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import server.cmdhandler.CmdHandlerFactory;
import server.cmdhandler.ICmdHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 */
@Slf4j
public class MainThreadProcessor {

    private static final MainThreadProcessor MAIN_THREAD_PROCESSOR = new MainThreadProcessor();

    private MainThreadProcessor() {
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
                        thread.setName("MainThread");
                        return thread;
                    },
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );

    public static MainThreadProcessor getInstance() {
        return MAIN_THREAD_PROCESSOR;
    }

    public void process(ChannelHandlerContext ctx, GeneratedMessageV3 msg) {
        if (ctx == null || msg == null) {
            return;
        }

        // 消息类型
        Class<? extends GeneratedMessageV3> msgClass = msg.getClass();

        ex.submit(() -> {
            // 通过 消息类型 获取对应的 处理对象
            ICmdHandler<? extends GeneratedMessageV3> cmdHandler = CmdHandlerFactory.getHandlerByClazz(msgClass);

            if (cmdHandler == null) {
                log.info("未找到对应的处理对象,msgClass = {}", msgClass);
                return;
            }

            try {
                cmdHandler.handle(ctx, cast(msg));
            } catch (Exception e) {
                // 防止发生异常导致线程终止
                log.error(e.getMessage(),e);
            }

        });

    }

    public void process(Runnable run){
        ex.submit(run);
    }

    /**
     * 转义消息对象
     * @param msg 消息对象
     * @param <TCmd>
     * @return
     */
    private <TCmd extends GeneratedMessageV3> TCmd cast(GeneratedMessageV3 msg) {
        if (msg == null || !(msg instanceof GeneratedMessageV3)) {
            return null;
        }
        return (TCmd) msg;
    }

}