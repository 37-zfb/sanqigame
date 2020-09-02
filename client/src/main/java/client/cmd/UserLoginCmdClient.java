package client.cmd;

import client.model.guild.PlayGuildClient;
import client.model.server.profession.Skill;
import client.model.server.props.Equipment;
import client.model.server.props.Potion;
import client.model.server.props.Props;
import client.model.server.scene.Monster;
import client.model.server.scene.Npc;
import client.model.server.scene.Scene;
import client.scene.GameData;
import client.thread.CmdThread;
import client.GameClient;
import client.model.MailClient;
import client.model.Role;
import client.model.SceneData;
import client.model.User;
import client.model.client.MailEntityClient;
import entity.db.UserEquipmentEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import msg.GameMsg;
import type.GuildMemberType;
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
                    new Skill(skill1.getId(), skill1.getProfessionId(), skill1.getName(), skill1.getCdTime(), skill1.getIntroduce(), skill1.getConsumeMp(), 0, skill1.getSkillProperty()));
        }


        Scene scene = SceneData.getInstance().getSceneMap().get(role.getCurrSceneId());
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
        List<GameMsg.Props> propsList = userLoginResult.getPropsList();
        Map<Integer, Props> backpackClient = role.getBackpackClient();
        for (GameMsg.Props props : propsList) {
            Props pro = propsMap.get(props.getPropsId());
            if (pro.getPropsProperty().getType() == PropsType.Equipment) {
                Equipment equipment = (Equipment) pro.getPropsProperty();

                Equipment propsProperty =
                        //props.getUserPropsId() 是 表 user_equipment 中的id
                        new Equipment(props.getUserPropsId(),
                                equipment.getPropsId(),
                                props.getDurability(),
                                equipment.getDamage(),
                                equipment.getEquipmentType());

                backpackClient.put(props.getLocation(), new Props(props.getPropsId(), pro.getName(), propsProperty));
            } else if (pro.getPropsProperty().getType() == PropsType.Potion) {
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

                backpackClient.put(props.getLocation(), new Props(props.getPropsId(), pro.getName(), propsProperty));
            }
        }


        // 穿戴装备
        UserEquipmentEntity[] userEquipmentEntityList = role.getUserEquipmentEntityArr();
        List<GameMsg.UserLoginResult.WearEquipment> wearEquList = userLoginResult.getWearEquList();
        for (int i = 0; i < wearEquList.size(); i++) {
            GameMsg.UserLoginResult.WearEquipment wearEquipment = wearEquList.get(i);
            userEquipmentEntityList[i] = new UserEquipmentEntity(wearEquipment.getId(), role.getId(), wearEquipment.getEquipmentId(), 1, wearEquipment.getDurability());

        }

        // 限量商品
        Map<Integer, Integer> goodsAllowNumber = role.getGOODS_ALLOW_NUMBER();
        List<GameMsg.UserLoginResult.GoodsLimit> goodLimitsList = userLoginResult.getGoodLimitsList();
        for (GameMsg.UserLoginResult.GoodsLimit goodsLimit : goodLimitsList) {
            goodsAllowNumber.put(goodsLimit.getGoodsId(), goodsLimit.getGoodsNumber());
        }

        // 初始化邮件系统
        initMail(role, userLoginResult);

        //公会
        String guildName = userLoginResult.getGuildName();
        int guildPosition = userLoginResult.getGuildPosition();
        if (!(guildName.equals("") || guildPosition == 0)) {
            PlayGuildClient playGuildClient = new PlayGuildClient();
            playGuildClient.setType(GuildMemberType.getRoleNameByRoleId(guildPosition));
            playGuildClient.setGuildName(guildName);
            role.setPlayGuildClient(playGuildClient);
        }


        // 下一步 操作
        CmdThread.getInstance().process(ctx, role, scene.getNpcMap().values());
    }


    private void initMail(Role role, GameMsg.UserLoginResult userLoginResult) {
        // 初始化邮件系统
        MailClient mail = role.getMail();
        Map<Long, MailEntityClient> mailMap = mail.getMailMap();
        List<GameMsg.MailInfo> mailInfoList = userLoginResult.getMailInfoList();
        for (GameMsg.MailInfo mailInfo : mailInfoList) {
            mailMap.put(mailInfo.getMailId(), new MailEntityClient(mailInfo.getMailId(), mailInfo.getSrcUserName(), mailInfo.getTitle()));
        }
        if (mailMap.size() > 0) {
            mail.setHave(true);
        }
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
