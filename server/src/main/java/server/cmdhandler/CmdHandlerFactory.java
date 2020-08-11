package server.cmdhandler;

import com.google.protobuf.GeneratedMessageV3;
import lombok.extern.slf4j.Slf4j;
import model.profession.skill.AbstractSkillProperty;
import server.GameServer;
import server.cmdhandler.skillhandler.ISkillHandler;
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

    // 消息类型 ==> 处理类
    /**
     * 类型 ==> 处理类
     */
    private static final Map<Class<?>, ICmdHandler<? extends GeneratedMessageV3>> HANDLER_MAP = new HashMap<>();


    private static final Map<Class<?>,ISkillHandler<? extends AbstractSkillProperty>> SKILL_HANDLER_MAP = new HashMap<>();

    private CmdHandlerFactory() {
    }

    /**
     * 关联 处理类、消息类型
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
                if (parameterTypes.length < 2 ||
                        parameterTypes[1] == GeneratedMessageV3.class ||
                        !GeneratedMessageV3.class.isAssignableFrom(parameterTypes[1])) {
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
                ICmdHandler<?> cmdHandler = (ICmdHandler<?>) GameServer.APPLICATION_CONTEXT.getBean(subClazz);
                log.info("关联 {} <==> {} ", msgType.getName(), subClazz.getName());
                HANDLER_MAP.put(msgType, cmdHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        initSkillHandler();
    }


    public static void initSkillHandler(){
        log.info("=== 完成 SkillCmd 和 Handler 的关联! ===");
        // 获取 Handler 类
        Set<Class<?>> listSubClazz = PackageUtil.listSubClazz(
                ISkillHandler.class.getPackage().getName(),
                true,
                ISkillHandler.class
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

                if (!"skillHandle".equals(clazzMethod.getName())) {
                    continue;
                }
                Class<?>[] parameterTypes = clazzMethod.getParameterTypes();
                if (parameterTypes.length < 2 ||
                        parameterTypes[1] == AbstractSkillProperty.class ||
                        !AbstractSkillProperty.class.isAssignableFrom(parameterTypes[1])) {
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
                ISkillHandler<?> cmdHandler = (ISkillHandler<?>) GameServer.APPLICATION_CONTEXT.getBean(subClazz);
                log.info("关联 {} <==> {} ", msgType.getName(), subClazz.getName());
                SKILL_HANDLER_MAP.put(msgType, cmdHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    /**
     * 通过消息类型获得对应的处理类
     *
     * @param clazz
     * @return
     */
    public static ICmdHandler<? extends GeneratedMessageV3> getHandlerByClazz(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return HANDLER_MAP.get(clazz);
    }

    /**
     * 通过技能消息类型获得对应的处理类
     *
     * @param clazz
     * @return
     */
    public static ISkillHandler<? extends AbstractSkillProperty> getSkillHandlerByClazz(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return SKILL_HANDLER_MAP.get(clazz);
    }
}
