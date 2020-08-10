package util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 张丰博
 */
@Slf4j
public class CustomizeThreadFactory implements ThreadFactory {

    private final AtomicInteger threadNumber = new AtomicInteger(0);

    private String namePrefix;

    public CustomizeThreadFactory(String baseName) {
        namePrefix = baseName + "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, namePrefix+threadNumber.getAndIncrement());
    }
}
