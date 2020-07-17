package server.async;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import server.MySqlSessionFactory;
import server.dao.UserDAO;
import server.entity.UserEntity;

import java.util.function.Function;

/**
 * @author 张丰博
 */
@Slf4j
public class AsyncService {

    private static AsyncService asyncService = new AsyncService();

    private AsyncService() {
    }

    public static AsyncService getInstance() {
        return asyncService;
    }

    public void asyn(String userName, String password, boolean isLogin, Function<UserEntity, Void> callback) {
        if (userName == null || password == null) {
            return;
        }

        AsyncOperation asyncOper = new AsyncOperation(userName, password, isLogin) {
            @Override
            public void doFinish() {
                if (callback != null) {
                    // 执行回调函数
                    callback.apply(this.getUserEntity());
                }
            }
        };

        AsyncOperationProcessor.getInstance().process(asyncOper);


    }

    private class AsyncOperation implements IAsyncOperation {

        private String username;

        private String password;

        private boolean isLogin;

        private UserEntity userEntity = null;

        public AsyncOperation(String username, String password, boolean isLogin) {
            if (username == null || password == null) {
                throw new RuntimeException("不合法的参数!");
            }
            this.username = username;
            this.password = password;
            this.isLogin = isLogin;
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
            SqlSession sqlSession = null;
            try {
                sqlSession = MySqlSessionFactory.openSession();
                log.info("sql连接:{}",sqlSession);
                UserDAO userDAO = sqlSession.getMapper(UserDAO.class);
                UserEntity entity = new UserEntity();
                if (isLogin) {
                    // 登录
                    entity = userDAO.getUserByName(username);
                    if (!password.equals(entity.getPassword())) {
                        log.error("用户密码错误,用户名：{}", username);
                        return;
                    }

                } else {
                    // 注册
                    entity = new UserEntity();
                    entity.setUserName(username);
                    entity.setPassword(password);

                    int id = userDAO.insertInto(entity);
                    entity.setId(id);
                    entity.setPassword(null);
                }
                userEntity = entity;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                if (sqlSession != null) {
                    sqlSession.close();
                }
            }


        }


        @Override
        public int getBindId() {
            return username.charAt(username.length()-1);
        }
    }


}
