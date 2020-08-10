package server;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;

/**
 * @author 张丰博
 */
@Slf4j
public class MySqlSessionFactory {

    private static SqlSessionFactory sqlSessionFactory;

    private MySqlSessionFactory() {
    }

    public static void init() {
        try {
            sqlSessionFactory =
                    (new SqlSessionFactoryBuilder()).build(Resources.getResourceAsReader("MyBatisConfig.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SqlSession openSession(){
        if (sqlSessionFactory == null){
            log.info("sqlSessionFactory 还未初始化!");
        }

        return sqlSessionFactory.openSession(true);
    }

}
