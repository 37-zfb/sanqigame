package server.cmdhandler;

import com.google.protobuf.GeneratedMessageV3;
import lombok.extern.slf4j.Slf4j;
import util.PackageUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author 张丰博
 * <p>
 * 处理类工厂
 */
@Slf4j
public class CmdHandlerFactory {

    private static final Map<Class<?>, ICmdHandler<? extends GeneratedMessageV3>> HANDLER_MAP = new HashMap<>();

    private CmdHandlerFactory() {
    }

    /**
     *  关联 处理类、消息类型
     */
    public static void init() {
        log.info("=== 完成 Cmd 和 Handler 的关联! ===");

        // 获取 Handler 类
        Set<Class<?>> listSubClazz = PackageUtil.listSubClazz(
                ICmdHandler.class.getPackage().getName(),
                true,
                ICmdHandler.class
        );

        for (Class<?> subClazz : listSubClazz) {
            // 判断是否是抽象类
            if (Modifier.isAbstract(subClazz.getModifiers())) {
                continue;
            }

            // 获取方法
            Method[] clazzMethods = subClazz.getMethods();

            Class<?> msgType = null;
            for (Method clazzMethod : clazzMethods) {

                if (!"handle".equals(clazzMethod.getName())) {
                    continue;

                }
                Class<?>[] parameterTypes = clazzMethod.getParameterTypes();
                if (parameterTypes.length < 2 || !GeneratedMessageV3.class.isAssignableFrom(parameterTypes[1])) {
                    continue;
                }

                msgType = parameterTypes[1];
                break;
            }
            if (msgType == null) {
                continue;
            }

            //创建 消息处理类
            try {
                ICmdHandler<?> cmdHandler = (ICmdHandler<?>) subClazz.newInstance();
                log.info("关联 {} <==> {} ", msgType.getName(), subClazz.getName());

                HANDLER_MAP.put(msgType, cmdHandler);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }


    }

    /**
     *  通过消息类型获得对应的处理类
     * @param clazz
     * @return
     */
    public static ICmdHandler<? extends GeneratedMessageV3> getHandlerByClazz(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return HANDLER_MAP.get(clazz);
    }
}
