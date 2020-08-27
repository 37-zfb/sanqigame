package server;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import msg.GameMsgRecognizer;
import server.cmdhandler.CmdHandlerFactory;
import server.cmdhandler.ICmdHandler;

import java.util.Random;
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

    /**
     * 创建线程组
     */
    private final ExecutorService[] ex = new ThreadPoolExecutor[8];

    private MainThreadProcessor() {
        for (int i = 0; i < ex.length; i++) {
            String threadName = "MainThread-" + i;
            ex[i] = new ThreadPoolExecutor(
                    1,
                    1,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    (newRunnable) -> {
                        Thread thread = new Thread(newRunnable);
                        thread.setName(threadName);
                        return thread;
                    },
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );
        }
    }

    public static MainThreadProcessor getInstance() {
        return MAIN_THREAD_PROCESSOR;
    }

    public void process(ChannelHandlerContext ctx, GeneratedMessageV3 msg) {
        if (ctx == null || msg == null) {
            return;
        }

        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();

        // 消息类型
        Class<? extends GeneratedMessageV3> msgClass = msg.getClass();
        ExecutorService ex;
        if (userId == null) {
            // 此时用户未登录
            Random random = new Random();
            int randomInt = random.nextInt(8);
            ex = this.ex[randomInt];
        } else {
            ex = this.ex[userId % this.ex.length];
        }

        ex.submit(() -> {
            log.info("当前线程:{}", Thread.currentThread().getName());

            // 通过 消息类型 获取对应的 处理对象
            ICmdHandler<? extends GeneratedMessageV3> cmdHandler = CmdHandlerFactory.getHandlerByClazz(msgClass);

            if (cmdHandler == null) {
                log.info("未找到对应的处理对象,msgClass = {}", msgClass);
                return;
            }

            try {
                cmdHandler.handle(ctx, cast(msg));
            } catch (CustomizeException e) {
                log.error(e.getMessage(), e);
                GameMsg.ErrorResult errorResult = GameMsg.ErrorResult.newBuilder()
                        .setCode(e.getCode())
                        .setMsg(e.getMessage())
                        .build();
                ctx.writeAndFlush(errorResult);
            } catch (Exception e) {
                // 防止发生异常导致线程终止
                log.error(e.getMessage(), e);
                GameMsg.ErrorResult errorResult = GameMsg.ErrorResult.newBuilder()
                        .setMsg(e.getMessage())
                        .build();
                ctx.writeAndFlush(errorResult);
            }

        });

    }


    /**
     * 转义消息对象
     *
     * @param msg    消息对象
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
