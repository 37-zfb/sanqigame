package server.async;

import entity.db.UserEntity;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.model.User;
import server.service.TaskService;
import server.service.UserService;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author 张丰博
 */
@Service
@Slf4j
public class RegisterService {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    public void asyn(UserEntity userEntity, ChannelHandlerContext ctx, Supplier<Void> callback) {
        if (userEntity == null || callback == null) {
            return;
        }

        AsyncRegister asyncOper = new AsyncRegister(userEntity) {
            @Override
            public void doFinish() {
                if (callback != null) {
                    // 执行回调函数
                    callback.get();
                }
            }
        };

        AsyncOperationProcessor.getInstance().process(asyncOper, ctx);


    }

    private class AsyncRegister implements IAsyncOperation {

        private UserEntity userEntity = null;

        public AsyncRegister(UserEntity userEntity) {
            if (userEntity == null) {
                throw new RuntimeException("不合法的参数!");
            }
            this.userEntity = userEntity;
        }


        /**
         * 异步执行
         */
        @Override
        public void doAsyn() {
            log.info("当前线程始:{}", Thread.currentThread().getName());
            userService.addUser(userEntity);
            taskService.addTask(userEntity.getId());
        }


        @Override
        public int getBindId() {
            return userEntity.getUserName().charAt(userEntity.getUserName().length() - 1);
        }
    }


}
