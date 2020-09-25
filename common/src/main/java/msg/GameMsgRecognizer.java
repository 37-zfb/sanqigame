package msg;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 张丰博
 */
@Slf4j
public class GameMsgRecognizer {

    /**
     * 消息编号 ==> 消息类型对象
     */
    private static final Map<Integer, GeneratedMessageV3> msgCodeAndMsgMap = new HashMap<>();

    /**
     * 消息类型 ==> 消息编号
     */
    private static final Map<Class<?>, Integer> msgClassAndMsgCodeMap = new HashMap<>();

    private GameMsgRecognizer() {
    }

    public static void init() {
        Class<?>[] innerClassArr = GameMsg.class.getDeclaredClasses();

        for (Class<?> innerClass : innerClassArr) {

            if (innerClass == null || !GeneratedMessageV3.class.isAssignableFrom(innerClass)) {
                continue;
            }
            // 类名称转换成小写
            String className = innerClass.getSimpleName();
            className = className.toLowerCase();

            for (GameMsg.MsgCode msgCode : GameMsg.MsgCode.values()) {
                // 枚举名称去下划线转小写
                String msgCodeName = msgCode.name();
                msgCodeName = msgCodeName.replaceAll("_", "");
                msgCodeName = msgCodeName.toLowerCase();

                if (!msgCodeName.startsWith(className)) {
                    continue;
                }

                try {
                    Object defaultInstance = innerClass.getDeclaredMethod("getDefaultInstance").invoke(innerClass);

                    log.info("关联 {} <==> {}", innerClass.getName(), msgCode.getNumber());

                    // 消息编号  消息对象
                    msgCodeAndMsgMap.put(msgCode.getNumber(), (GeneratedMessageV3) defaultInstance);

                    msgClassAndMsgCodeMap.put(innerClass, msgCode.getNumber());
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    log.error(e.getMessage(), e);
                }


            }


        }

    }


    /**
     * 根据消息编号 获取 消息构建者
     *
     * @param msgCode 消息编号
     * @return 构建者
     */
    public static Message.Builder getMsgBuilderByMsgCode(int msgCode) {
        if (msgCode < 0) {
            return null;
        }
        GeneratedMessageV3 messageV3 = msgCodeAndMsgMap.get(msgCode);
        if (messageV3 == null) {
            return null;
        }
        return messageV3.newBuilderForType();
    }

    /**
     * 根据消息类型获取消息编号
     *
     * @param clazz
     * @return
     */
    public static int getMsgCodeByMsgClass(Class<?> clazz) {
        if (clazz == null) {
            return -1;
        }

        Integer msgCode = msgClassAndMsgCodeMap.get(clazz);
        if (msgCode == null) {
            msgCode = -1;
        }
        return msgCode.intValue();
    }


}
