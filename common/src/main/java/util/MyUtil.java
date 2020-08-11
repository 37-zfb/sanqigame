package util;

import com.google.protobuf.GeneratedMessageV3;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import org.apache.ibatis.jdbc.Null;

/**
 * @author 张丰博
 *
 */
public final class MyUtil {

    private MyUtil(){}

    public static void checkIsNull(ChannelHandlerContext ctx, GeneratedMessageV3 cmd){
        if (ctx == null || cmd == null){
            throw new NullPointerException();
        }
    }


}
