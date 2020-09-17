package client.cmd.task;

import client.cmd.ICmd;
import client.model.Role;
import client.model.server.props.Equipment;
import client.model.server.props.Props;
import client.model.server.task.Task;
import client.scene.GameData;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 */
public class ReceiveTaskAwardResultClient implements ICmd<GameMsg.ReceiveTaskAwardResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.ReceiveTaskAwardResult receiveTaskAwardResult) {

        MyUtil.checkIsNull(ctx, receiveTaskAwardResult);
        Role role = Role.getInstance();




        int rewardMoney = receiveTaskAwardResult.getMoney();
        List<GameMsg.Props> propsList = receiveTaskAwardResult.getPropsList();

        role.setMoney(role.getMoney() + rewardMoney);

        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        Map<Integer, Props> backpackClient = role.getBackpackClient();
        for (GameMsg.Props props : propsList) {
            Props p = backpackClient.get(props.getLocation());
            if (p == null) {
                //添加
                Props pro = propsMap.get(props.getPropsId());
                Equipment equipment = (Equipment) pro.getPropsProperty();

                Equipment propsProperty =
                        //props.getUserPropsId() 是 表 user_equipment 中的id
                        new Equipment(props.getUserPropsId(),
                                equipment.getPropsId(),
                                props.getDurability(),
                                equipment.getDamage(),
                                equipment.getEquipmentType());

                backpackClient.put(props.getLocation(), new Props(pro.getId(), pro.getName(), propsProperty));
            }
        }

        GameData gameData = GameData.getInstance();
        Task task = gameData.getTaskMap().get(role.getPlayTaskClient().getCurrTaskId());
        System.out.println("获得奖励: ");
        System.out.println(task.getExperience() + "经验");
        task.getRewardProps().forEach(System.out::println);
        System.out.println(task.getRewardMoney() + " 金币");

//        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
    }
}
