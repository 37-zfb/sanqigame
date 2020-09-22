package server.service;

import entity.db.DbAllSendMailEntity;
import entity.db.DbSendMailEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.dao.IAllSendMailDAO;
import server.dao.ISendMailDAO;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author 张丰博
 *
 * 全体邮件
 */
@Service
@Slf4j
public class AllSendMailService {

    @Autowired
    private IAllSendMailDAO allSendMailDAO;

    @Autowired
    private ISendMailDAO sendMailDAO;

    /**
     * 添加邮件
     * @param allSendMailEntity
     */
    public void addAllMail(DbAllSendMailEntity allSendMailEntity){
        if (allSendMailEntity == null){
            return;
        }

        allSendMailDAO.insertMail(allSendMailEntity);
    }

    /**
     * 查询所有全体邮件
     * @return
     */
    public List<DbAllSendMailEntity> listAllMail(){


        return allSendMailDAO.selectMail();
    }

    /**
     * 删除邮件
     * @param id
     */
    public void deleteMail(Integer id){
        if (id == null){
            return;
        }

        allSendMailDAO.deleteMail(id);
    }


}
