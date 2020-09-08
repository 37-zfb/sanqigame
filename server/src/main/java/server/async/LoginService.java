package server.async;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import entity.db.UserEntity;
import server.model.User;
import server.service.GuildService;
import server.service.MailService;
import server.service.TaskService;
import server.service.UserService;

import java.util.function.Function;

/**
 * @author 张丰博
 */
@Slf4j
@Service
public class LoginService {

    @Autowired
    private UserService userService;


    public void asyn(String userName, String password, ChannelHandlerContext ctx, Function<UserEntity, Void> callback) {
        if (userName == null || password == null) {
            return;
        }

        AsyncGetUserByName asyncOper = new AsyncGetUserByName(userName, password) {
            @Override
            public void doFinish() {
                if (callback != null) {
                    // 执行回调函数
                    callback.apply(this.getUserEntity());
                }
            }
        };
        AsyncOperationProcessor.getInstance().process(asyncOper, ctx);
    }

    private class AsyncGetUserByName implements IAsyncOperation {

        private String username;

        private String password;

        private UserEntity userEntity = null;


        public AsyncGetUserByName(String username, String password) {
            if (username == null || password == null) {
                throw new RuntimeException("不合法的参数!");
            }
            this.username = username;
            this.password = password;
        }

        public UserEntity getUserEntity() {
            return userEntity;
        }

        /**
         * 异步执行
         */
        @Override
        public void doAsyn() {
            log.info("当前线程始:{}", Thread.currentThread().getName());
            UserEntity userEntity = userService.getUserByName(username);
            this.userEntity = userEntity;
        }


        @Override
        public int getBindId() {
            return username.charAt(username.length() - 1);
        }
    }


}
