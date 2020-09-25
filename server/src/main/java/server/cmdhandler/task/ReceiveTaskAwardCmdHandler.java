package server.cmdhandler.task;

import entity.MailProps;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.cmdhandler.task.listener.TaskUtil;
import server.model.PlayTask;
import server.model.User;
import server.model.props.Props;
import server.model.task.Task;
import server.scene.GameData;
import server.util.PropsUtil;
import util.MyUtil;

import java.util.*;

/**
 * @author 张丰博
 * 获取任务奖励 并接新任务
 */
@Component
@Slf4j
public class ReceiveTaskAwardCmdHandler implements ICmdHandler<GameMsg.ReceiveTaskAwardCmd> {

    @Autowired
    TaskUtil taskPublicMethod;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.ReceiveTaskAwardCmd receiveTaskAwardCmd) {

        MyUtil.checkIsNull(ctx, receiveTaskAwardCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        int taskId = receiveTaskAwardCmd.getTaskId();
        Task task = GameData.getInstance().getTaskMap().get(taskId);
        if (task == null) {
            return;
        }

        PlayTask playTask = user.getPlayTask();

        if (!playTask.isCurrTaskCompleted()){
            //还未完成任务
            return;
        }

        playTask.setCompletedTaskId(playTask.getCurrTaskId());
        playTask.setCurrTaskCompleted(false);


        Integer rewardMoney = task.getRewardMoney();
        List<MailProps> rewardProps = task.getRewardProps();
        Integer experience = task.getExperience();


        // 添加奖励
        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        user.setMoney(user.getMoney() + rewardMoney);

        log.info("用户 {} 完成 {} 任务，获得 {} 经验，", user.getUserName(), task.getTaskName(), experience);
        if (rewardProps.size() != 0) {
            log.info("用户 {} 获得 {} 金币，获得 {} 号道具", user.getUserName(), rewardMoney, rewardProps.get(0).getPropsId());
        }

        Map<Integer, Props> backpack = user.getBackpack();

        GameMsg.ReceiveTaskAwardResult.Builder newBuilder = GameMsg.ReceiveTaskAwardResult.newBuilder();
        for (MailProps rewardProp : rewardProps) {
            Props props = propsMap.get(rewardProp.getPropsId());
            PropsUtil.getPropsUtil().addProps(Collections.singletonList(rewardProp.getPropsId()), user, newBuilder, rewardProp.getNumber());
        }

        newBuilder.setMoney(rewardMoney);

        GameMsg.ReceiveTaskAwardResult receiveTaskAwardResult = newBuilder.build();
        ctx.writeAndFlush(receiveTaskAwardResult);

        //添加经验
        taskPublicMethod.addExperience(experience, user);
        //领取新任务
        taskPublicMethod.receiveTask(ctx, taskId + 1);
    }
}
