package server.cmdhandler.task.listener;

import entity.db.UserEquipmentEntity;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import server.model.PlayTask;
import server.model.User;
import server.model.props.Props;
import server.model.task.Task;
import server.scene.GameData;
import type.PropsType;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 张丰博
 */
@Slf4j
public class TaskListener {


    private static final TaskListener TASK_LISTENER = new TaskListener();


    public final Map<Integer, Method> listenerMethod = new HashMap<>();

    private TaskListener() {
    }

    public void init() {
        Method[] declaredMethods = TaskListener.class.getDeclaredMethods();
        for (Task task : GameData.getInstance().getTaskMap().values()) {
            for (Method declaredMethod : declaredMethods) {
                if (declaredMethod.getName().contains(String.valueOf(task.getId()))) {
                    listenerMethod.put(task.getId(), declaredMethod);
                }
            }
        }
    }


    public static TaskListener getTaskListener() {
        return TASK_LISTENER;
    }

    /**
     * 任务2: 击杀森林怪
     *
     * @param user
     */
    public void killForestMonster1(User user) {
        Integer currTaskId = user.getPlayTask().getCurrTaskId();
        Task task = GameData.getInstance().getTaskMap().get(currTaskId);
        if (!(user.getCurSceneId().equals(task.getSceneId()) && currTaskId.equals(user.getPlayTask().getCurrTaskId()))) {
            //场景不对或任务id不对时，直接结束
            return;
        }

        PlayTask playTask = user.getPlayTask();
        playTask.setCurrTaskCompleted(true);


        log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());

        GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
        user.getCtx().writeAndFlush(taskCompletedResult);
    }

    /**
     * 任务2: 击杀森林怪
     *
     * @param user
     */
    public void killForestMonster2(User user) {
        Integer currTaskId = user.getPlayTask().getCurrTaskId();

        Task task = GameData.getInstance().getTaskMap().get(currTaskId);
        if (!(user.getCurSceneId().equals(task.getSceneId()) && currTaskId.equals(user.getPlayTask().getCurrTaskId()))) {
            //场景不对或任务id不对时，直接结束
            return;
        }
        PlayTask playTask = user.getPlayTask();
        playTask.setKillNumber(playTask.getKillNumber() + 1);

        //击杀个数达到任务要求
        if (playTask.getKillNumber().equals(task.getKillNumber())) {
            //完成任务
            playTask.setCurrTaskCompleted(true);

            log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());
            GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
            user.getCtx().writeAndFlush(taskCompletedResult);
        }
        log.info("用户 {} 已击杀 {} 只怪;", user.getUserName(), playTask.getKillNumber());
    }

    /**
     * 任务3: 加入公会
     *
     * @param user
     */
    public void addGuild3(User user) {

        if (user.getPlayGuild() != null) {
            //完成任务
            PlayTask playTask = user.getPlayTask();
            playTask.setCurrTaskCompleted(true);
            Task task = GameData.getInstance().getTaskMap().get(playTask.getCurrTaskId());

            log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());
            GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
            user.getCtx().writeAndFlush(taskCompletedResult);
        }

    }

    /**
     * 任务4: 通关领主之塔
     *
     * @param user
     */
    public void throughLordTower4(User user) {
        PlayTask playTask = user.getPlayTask();
        playTask.setCurrTaskCompleted(true);
        Task task = GameData.getInstance().getTaskMap().get(playTask.getCurrTaskId());

        log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());
        GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
        user.getCtx().writeAndFlush(taskCompletedResult);
    }

    /**
     * 任务5: 等级达到5级
     *
     * @param user
     */
    public void reachLv5(User user) {
        int targetLv = 5;
        if (user.getLv() >= targetLv) {
            PlayTask playTask = user.getPlayTask();
            playTask.setCurrTaskCompleted(true);
            Task task = GameData.getInstance().getTaskMap().get(playTask.getCurrTaskId());

            log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());
            GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
            user.getCtx().writeAndFlush(taskCompletedResult);
        }
    }

    /**
     * 任务6:获得8件极品装备
     *
     * @param user
     */
    public void getEqu6(User user) {

        int targetEquSize = 8;

        Map<Integer, Props> backpack = user.getBackpack();
        int equSize = 0;
        for (Props props : backpack.values()) {
            if (props.getPropsProperty().getType() == PropsType.Equipment) {
                equSize++;
            }
        }

        if (equSize < targetEquSize) {
            return;
        }

        PlayTask playTask = user.getPlayTask();
        playTask.setCurrTaskCompleted(true);
        Task task = GameData.getInstance().getTaskMap().get(playTask.getCurrTaskId());

        log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());
        GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
        user.getCtx().writeAndFlush(taskCompletedResult);
    }

    /**
     * 任务7:穿戴的9件装备
     *
     * @param user
     */
    public void wearEquReachNine7(User user) {

        int targetNumber = 9;
        int currSize = 0;
        for (UserEquipmentEntity userEquipmentEntity : user.getUserEquipmentArr()) {
            if (userEquipmentEntity != null) {
                currSize++;
            }
        }

        if (targetNumber != currSize) {
            return;
        }

        PlayTask playTask = user.getPlayTask();
        playTask.setCurrTaskCompleted(true);
        Task task = GameData.getInstance().getTaskMap().get(playTask.getCurrTaskId());

        log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());
        GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
        user.getCtx().writeAndFlush(taskCompletedResult);
    }

    /**
     * 任务8:添加好友
     *
     * @param user
     */
    public void addFriend8(User user) {


        PlayTask playTask = user.getPlayTask();
        playTask.setCurrTaskCompleted(true);
        Task task = GameData.getInstance().getTaskMap().get(playTask.getCurrTaskId());

        log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());
        GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
        user.getCtx().writeAndFlush(taskCompletedResult);
    }

    /**
     * 任务9:组队
     *
     * @param user
     */
    public void team9(User user) {

        if (user.getPlayTeam() == null) {
            return;
        }

        PlayTask playTask = user.getPlayTask();
        playTask.setCurrTaskCompleted(true);
        Task task = GameData.getInstance().getTaskMap().get(playTask.getCurrTaskId());

        log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());
        GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
        user.getCtx().writeAndFlush(taskCompletedResult);
    }

    /**
     * 任务10:与玩家交易
     *
     * @param user
     */
    public void deal10(User user) {

        PlayTask playTask = user.getPlayTask();
        playTask.setCurrTaskCompleted(true);
        Task task = GameData.getInstance().getTaskMap().get(playTask.getCurrTaskId());

        log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());
        GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
        user.getCtx().writeAndFlush(taskCompletedResult);
    }

    /**
     * 任务11: 在pk中战胜
     *
     * @param user
     */
    public void pk11(User user) {
        PlayTask playTask = user.getPlayTask();
        playTask.setCurrTaskCompleted(true);
        Task task = GameData.getInstance().getTaskMap().get(playTask.getCurrTaskId());

        log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());
        GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
        user.getCtx().writeAndFlush(taskCompletedResult);
    }

    /**
     * 任务12:金币达到10000
     *
     * @param user
     */
    public void reachMoney12(User user) {
        int targetMoney = 10000;
        if (user.getMoney() < targetMoney){
            return;
        }

            PlayTask playTask = user.getPlayTask();
        playTask.setCurrTaskCompleted(true);
        Task task = GameData.getInstance().getTaskMap().get(playTask.getCurrTaskId());

        log.info("用户 {} 完成 {} 任务", user.getUserName(), task.getTaskName());
        GameMsg.TaskCompletedResult taskCompletedResult = GameMsg.TaskCompletedResult.newBuilder().build();
        user.getCtx().writeAndFlush(taskCompletedResult);
    }


}
