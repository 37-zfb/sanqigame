package client.cmd;

import client.CmdThread;
import client.GameClient;
import client.model.Role;
import client.model.User;
import entity.db.UserEquipmentEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import model.profession.Skill;
import model.props.Equipment;
import model.props.Potion;
import model.props.Props;
import model.scene.Monster;
import model.scene.Npc;
import model.scene.Scene;
import msg.GameMsg;
import scene.GameData;
import type.PropsType;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author 张丰博
 */
@Slf4j
public class UserLoginCmdClient implements ICmd<GameMsg.UserLoginResult> {

    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserLoginResult userLoginResult) {

        if (ctx == null || userLoginResult == null) {
            return;
        }

        if (userLoginResult.getUserId() == -413) {
            System.out.println("用户名或密码错误,请重新输入");
            GameClient.cmdLogin(ctx.channel());
            return;
        }

        // 取出服务的数据，构建Role
        Role role = Role.getInstance();
        role.setId(userLoginResult.getUserId());
        role.setUserName(userLoginResult.getUserName());
        role.setCurrHp(userLoginResult.getHp());
        role.setCurrMp(userLoginResult.getMp());
        role.setCurrSceneId(userLoginResult.getCurrSceneId());
        role.setProfessionId(userLoginResult.getProfessionId());
        role.setMoney(userLoginResult.getMoney());

        role.getUserResumeState().setEndTimeMp(userLoginResult.getResumeMpEndTime());
        // 如果mp不满，则自动恢复
        role.startResumeMp();

        // 封装 当前用户的技能
        List<GameMsg.UserLoginResult.Skill> skillListResult = userLoginResult.getSkillList();
        Map<Integer, Skill> skillMap = role.getSkillMap();
        Map<Integer, Skill> professionSkillMap = GameData.getInstance().getProfessionMap().get(role.getProfessionId()).getSkillMap();
        for (GameMsg.UserLoginResult.Skill skill : skillListResult) {
            // 通过技能id
            Skill skill1 = professionSkillMap.get(skill.getId());
            skillMap.put(skill.getId(),
                    new Skill(skill1.getId(),skill1.getProfessionId(),skill1.getName(),skill1.getCdTime(),skill1.getIntroduce(),skill1.getConsumeMp(),0,skill1.getSkillProperty()));
        }


        Scene scene = GameData.getInstance().getSceneMap().get(role.getCurrSceneId());

        // 封装当前场景的 npc
        List<GameMsg.UserLoginResult.Npc> npcList = userLoginResult.getNpcList();
        Map<Integer, Npc> npcMap = scene.getNpcMap();
        for (GameMsg.UserLoginResult.Npc npc : npcList) {
            npcMap.put(npc.getId(), new Npc(npc.getId(), npc.getName(), role.getCurrSceneId(), npc.getInfo()));
        }

        // 封装当前场景的 怪
        List<GameMsg.UserLoginResult.Monster> monsterList = userLoginResult.getMonsterList();
        Map<Integer, Monster> monsterMap = scene.getMonsterMap();
        for (GameMsg.UserLoginResult.Monster monster : monsterList) {
            monsterMap.put(monster.getId(), new Monster(monster.getId(), role.getCurrSceneId(), monster.getName(), monster.getHp()));
        }

        //封装背包中的物品
        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        List<GameMsg.UserLoginResult.Props> propsList = userLoginResult.getPropsList();
        Map<Integer, Props> backpackClient = role.getBackpackClient();
        for (GameMsg.UserLoginResult.Props props : propsList) {
            Props pro = propsMap.get(props.getPropsId());
            if (pro.getPropsProperty().getType() == PropsType.Equipment){
                Equipment equipment = (Equipment) pro.getPropsProperty();

                Equipment propsProperty =
                        //props.getUserPropsId() 是 表 user_equipment 中的id
                        new Equipment(props.getUserPropsId(),
                                equipment.getPropsId(),
                                props.getDurability(),
                                equipment.getDamage(),
                                equipment.getEquipmentType());

                backpackClient.put(props.getLocation(), new Props(props.getPropsId(), pro.getName(), propsProperty));
            }else if (pro.getPropsProperty().getType() == PropsType.Potion){
                Potion potion = (Potion) pro.getPropsProperty();

                Potion propsProperty =
                        //props.getUserPropsId() 是 表 user_potion 中的id
                        new Potion(props.getUserPropsId(),
                                potion.getPropsId(),
                                potion.getCdTime(),
                                potion.getInfo(),
                                potion.getResumeFigure(),
                                potion.getPercent(),
                                props.getPropsNumber());

                backpackClient.put(props.getLocation(),new Props(props.getPropsId(),pro.getName(), propsProperty));
            }
        }



        // 穿戴装备
        UserEquipmentEntity[] userEquipmentEntityList = role.getUserEquipmentEntityArr();
        List<GameMsg.UserLoginResult.WearEquipment> wearEquList = userLoginResult.getWearEquList();
        for (int i = 0; i < wearEquList.size(); i++) {
            GameMsg.UserLoginResult.WearEquipment wearEquipment = wearEquList.get(i);
            userEquipmentEntityList[i] = new UserEquipmentEntity(wearEquipment.getId(), role.getId(), wearEquipment.getEquipmentId(), 1, wearEquipment.getDurability());

        }


        // 下一步 操作

        CmdThread.getInstance().process(ctx, role, scene.getNpcMap().values());
    }


    /**
     * 登录
     */
    public User login() {
        Scanner scanner = new Scanner(System.in);
        String userName = null;
        String password = null;
        while (true) {
            System.out.print("============登录:请输入您的用户名: ");
            userName = scanner.nextLine();

            if ("".equals(userName)) {
                log.error("登录:用户名不能为空,请重新输入!");
                continue;
            }

            System.out.print("============登录:请输入您的密码: ");
            password = scanner.nextLine();

            if ("".equals(password)) {
                log.error("登录:密码不能为空,请重新输入!");
                continue;
            }
            break;
        }
        return new User(userName, password);
    }


}
