package server.async;

import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import server.MainThreadProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 张丰博
 * 异步处理器
 */
@Slf4j
public class AsyncOperationProcessor {

    private static final AsyncOperationProcessor ASYNC_OPERATION_PROCESSOR = new AsyncOperationProcessor();


    /**
     * 创建线程组
     */
    private final ExecutorService[] ex = new ThreadPoolExecutor[8];

    /**
     * 初始化线程组
     */
    private AsyncOperationProcessor() {
        for (int i = 0; i < ex.length; i++) {
            String threadName = "AsyncOperationProcessor-" + i;
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

    public static AsyncOperationProcessor getInstance() {
        return ASYNC_OPERATION_PROCESSOR;
    }

    public void process(IAsyncOperation asyncOperation, ChannelHandlerContext ctx) {
        if (asyncOperation == null) {
            return;
        }

        ex[asyncOperation.getBindId() % 8].submit(() -> {
            try {
                asyncOperation.doAsyn();
                MainThreadProcessor.getInstance().process(ctx, () -> {

                    try {

                        asyncOperation.doFinish();


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
                                .setCode(-99999)
                                .setMsg("服务端未知异常;")
                                .build();
                        ctx.writeAndFlush(errorResult);
                    }
                });

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
                        .setCode(-99999)
                        .setMsg("服务端未知异常;")
                        .build();
                ctx.writeAndFlush(errorResult);
            }

        });


    }


}
