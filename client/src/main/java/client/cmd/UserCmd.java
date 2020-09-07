package client.cmd;

import client.model.MailClient;
import client.model.PlayUserClient;
import client.model.Role;
import client.model.client.MailEntityClient;
import client.model.server.duplicate.Duplicate;
import client.model.server.profession.Skill;
import client.model.server.props.Equipment;
import client.model.server.props.Potion;
import client.model.server.props.Props;
import client.model.server.scene.Npc;
import client.model.server.scene.Scene;
import client.model.server.store.Goods;
import client.model.server.task.Task;
import client.model.task.PlayTaskClient;
import client.scene.GameData;
import client.thread.BossThread;
import client.thread.DealThread;
import com.sun.org.apache.regexp.internal.RE;
import entity.db.UserEquipmentEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import msg.GameMsg;
import type.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

/**
 * @author 张丰博
 */
@Slf4j
public class UserCmd {


    /**
     * @param role    角色对象
     * @param npcList npc集合
     * @return
     */
    public static Object operation(Role role, Collection<Npc> npcList, ChannelHandlerContext ctx) {
        // 构建角色，角色初始在启始之地
        log.info("============欢迎勇士来到冒险大陆==============");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                Integer currSceneId = role.getCurrSceneId();
                Scene scene = GameData.getInstance().getSceneMap().get(currSceneId);
                String curSceneName = scene.getName();
                log.info("============所在地: {} ==============", scene.getName());
//                log.info("============当前场景的npc个数: {}", (npcList == null ? 0 : npcList.size()));
//                if (npcList != null) {
//                    for (Npc npc : npcList) {
//                        log.info("===>> npc 名称: {}", npc.getName());
//                    }
//                }

                if (role.getTEAM_CLIENT().getTeamLeaderId() != null) {
                    log.info("======> 队伍成员如下: ");
                    for (PlayUserClient playUserClient : role.getTEAM_CLIENT().getTeamMember()) {
                        if (playUserClient != null && playUserClient.getUserId().equals(role.getTEAM_CLIENT().getTeamLeaderId())) {
                            log.info("队长: {}", playUserClient.getUserName());
                        } else if (playUserClient != null) {
                            log.info("队员: {}", playUserClient.getUserName());
                        }
                    }
                }

                if (role.getMail().isHave()) {
                    log.info("======> {}", "存在未领取的邮件");
                }
                PlayTaskClient playTaskClient = role.getPlayTaskClient();
                if (playTaskClient.getCurrTaskId() != null){
                    Map<Integer, Task> taskMap = GameData.getInstance().getTaskMap();
                    log.info("任务: {}",taskMap.get(playTaskClient.getCurrTaskId()).getTaskName());
                }

                while (true) {
                    log.info("请选择您的操作: ");
                    System.out.println("======>1:切换场景;");
                    System.out.println("======>2:当前场景所有实体;");
                    System.out.println("======>3:普通攻击;");
                    System.out.println("======>4:技能列表;");
                    System.out.println("======>5:展示NPC;");
                    System.out.println("======>6:使用MP/HP药剂;");
                    System.out.println("======>7:背包;");
                    System.out.println("======>8:装备栏;");
                    System.out.println("======>9:怪发起攻击;");
                    System.out.println("======>10:穿戴装备;");
                    System.out.println("======>11:卸下装备;");
                    System.out.println("======>12:修理装备;");
                    System.out.println("======>13:副本;");
                    System.out.println("======>14:商店;");
                    System.out.println("======>15:聊天;");
                    System.out.println("======>16:发送邮件;");
                    System.out.println("======>17:邮箱;");
                    System.out.println("======>18:进入竞技场;");
                    System.out.println("======>19:组队;");
                    System.out.println("======>20:加入队伍;");
                    System.out.println("======>21:不加队伍;");
                    System.out.println("======>22:退出队伍;");
                    System.out.println("======>23:跟随队伍进入副本;");
                    System.out.println("======>24:交易;");
                    System.out.println("======>25:同意交易;");
                    System.out.println("======>26:拒绝交易;");
                    System.out.println("======>27:进入交易界面;");
                    System.out.println("======>28:公会;");
                    System.out.println("======>29:拍卖行;");
                    System.out.println("======>30:查看任务;");
                    System.out.println("======>99:退出;");

                    // 操作指令数字
                    String command = scanner.next();
                    scanner.nextLine();

                    if ("1".equals(command)) {
                        // 移动场景
                        System.out.print("目标场景: ");
                        String targetScene = scanner.nextLine();

                        Integer targetId;
                        if ("村子".equalsIgnoreCase(targetScene) && "启始之地、森林、城堡".contains(curSceneName)) {
                            targetId = 2;
                        } else if ("启始之地".equalsIgnoreCase(targetScene) && "村子".contains(curSceneName)) {
                            targetId = 1;
                        } else if ("森林".equalsIgnoreCase(targetScene) && "村子".contains(curSceneName)) {
                            targetId = 3;
                        } else if ("城堡".equalsIgnoreCase(targetScene) && "村子".contains(curSceneName)) {
                            targetId = 4;
                        } else {
                            log.info("{} 和 {} 不相邻!", targetScene, curSceneName);
                            continue;
                        }
                        return targetId;
                    } else if ("2".equals(command)) {
                        // 当前场景实体
                        return GameMsg.WhoElseIsHereCmd.newBuilder().build();
                    } else if ("3".equals(command)) {
                        // 普通攻击
                        GameMsg.AttkCmd.Builder cmdBuilder = GameMsg.AttkCmd.newBuilder();
                        return cmdBuilder;
                    } else if ("4".equals(command)) {
                        System.out.println("当前所拥有技能如下: ");
                        for (Skill skill : role.getSkillMap().values()) {
                            System.out.println(skill.getId() + "、技能 名称: " + skill.getName() + " , \tcd: " + skill.getCdTime() + " , 是否冷却中: " + (skill.isCd() ? "冷却中;" : "未冷却;"));
                        }
                        int skillId = scanner.nextInt();

                        GameMsg.UserSkillAttkCmd userSkillAttkCmd = GameMsg.UserSkillAttkCmd.newBuilder()
                                .setSkillId(skillId)
                                .build();

                        return userSkillAttkCmd;

                    } else if ("5".equals(command)) {

                        if (npcList.size() != 0) {
                            System.out.println("当前场景NPC如下: ");
                            for (Npc npc : npcList) {
                                System.out.println(npc.getId() + "、名称: " + npc.getName());
                            }
                            Integer npcId = scanner.nextInt();
                            if (role.getPlayTaskClient().getCurrTaskId() != null &&
                                    npcId.equals(GameData.getInstance().getTaskMap().get(role.getPlayTaskClient().getCurrTaskId()).getNpcId()) ){
                                //此时任务，和当前npc对话
                                //发送协议，当前位置
                                return GameMsg.DialogueTaskCmd.newBuilder().build();
                            }else {
                                Npc npc = scene.getNpcMap().get(npcId);
                                System.out.println(npc.getName() + ": " + npc.getInfo());
                            }

                        } else {
                            System.out.println("此场景没有NPC!");
                        }

                    } else if ("6".equals(command)) {
                        // 人物
                        System.out.println("药剂如下:");
                        Map<Integer, Props> backpackClient = role.getBackpackClient();

                        for (Map.Entry<Integer, Props> propsEntry : backpackClient.entrySet()) {
                            if (propsEntry.getValue().getPropsProperty().getType() == PropsType.Potion) {
                                Potion p = (Potion) propsEntry.getValue().getPropsProperty();
                                System.out.println("==> " + propsEntry.getKey() + "、" + propsEntry.getValue().getName() + "\t cd: " + p.getCdTime() + "\t 描述: " + p.getInfo() + "\t 数量: " + p.getNumber());
                            }
                        }

                        // 药剂 所在位置
                        int location = scanner.nextInt();
                        GameMsg.UserPotionCmd potionCmd = GameMsg.UserPotionCmd.newBuilder().setLocation(location).build();
                        return potionCmd;
                    } else if ("7".equals(command)) {
                        //背包
                        Map<Integer, Props> backpackClient = role.getBackpackClient();
                        System.out.println("背包空间: " + backpackClient.size() + "/100");
                        System.out.println("道具如下:");
                        System.out.println("金币: " + role.getMoney());
                        for (Map.Entry<Integer, Props> propsEntry : backpackClient.entrySet()) {
                            if (propsEntry.getValue().getPropsProperty().getType() == PropsType.Equipment) {
                                System.out.println("==> " + propsEntry.getKey() + "、" + propsEntry.getValue().getName() + "\t\t类型: " + propsEntry.getValue().getPropsProperty().getType().getType());
                            } else if (propsEntry.getValue().getPropsProperty().getType() == PropsType.Potion) {
                                Potion potion = (Potion) propsEntry.getValue().getPropsProperty();
                                System.out.println("==> " + propsEntry.getKey() + "、" + propsEntry.getValue().getName() + "\t\t类型: " + propsEntry.getValue().getPropsProperty().getType().getType() + " \t\t数量: " + potion.getNumber());
                            }
                        }


                    } else if ("9".equals(command)) {
                        // 怪发起攻击
                        GameMsg.MonsterStartAttkUser.Builder builder = GameMsg.MonsterStartAttkUser.newBuilder();
                        return builder;
                    } else if ("99".equals(command)) {
                        ctx.channel().close();
                        System.exit(0);
                    } else if ("8".equals(command)) {
                        //装备栏
                        UserEquipmentEntity[] userEquipmentEntityList = role.getUserEquipmentEntityArr();
                        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
                        System.out.println("已穿戴的装备如下:");
                        for (UserEquipmentEntity userEquipmentEntity : userEquipmentEntityList) {
                            if (userEquipmentEntity != null) {
                                Props props = propsMap.get(userEquipmentEntity.getPropsId());
                                Equipment equipment = (Equipment) props.getPropsProperty();
                                if (equipment.getEquipmentType() == EquipmentType.Weapon) {
                                    System.out.println("类型: " + equipment.getEquipmentType().getType() + " 名称: " + props.getName() + " 耐久度: " + userEquipmentEntity.getDurability());
                                } else {
                                    System.out.println("类型: " + equipment.getEquipmentType().getType() + " 名称: " + props.getName());
                                }
                            }
                        }
                    } else if ("10".equals(command)) {
                        // 穿戴装备
                        Map<Integer, Props> backpackClient = role.getBackpackClient();

                        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
                        System.out.println("装备如下:");
                        for (Map.Entry<Integer, Props> integerPropsEntry : backpackClient.entrySet()) {
                            Props props = integerPropsEntry.getValue();
                            if (props.getPropsProperty().getType() == PropsType.Equipment) {
                                Equipment equipment = (Equipment) props.getPropsProperty();
                                System.out.println("==> " + integerPropsEntry.getKey() + "、" + props.getName() + "、 " + equipment.getEquipmentType().getType());
                            }
                        }


                        // 装备位置
                        int location = scanner.nextInt();
                        Props props = backpackClient.get(location);
                        Equipment equipment = (Equipment) props.getPropsProperty();
                        return GameMsg.UserWearEquipmentCmd.newBuilder().setLocation(location).setUserEquipmentId(equipment.getId()).build();

                    } else if ("11".equals(command)) {
                        //卸装备
                        //装备栏
                        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
                        UserEquipmentEntity[] userEquipmentEntityList = role.getUserEquipmentEntityArr();
                        System.out.println("已穿戴的装备如下:");
                        for (UserEquipmentEntity userEquipmentEntity : userEquipmentEntityList) {
                            if (userEquipmentEntity != null) {
                                Props props = propsMap.get(userEquipmentEntity.getPropsId());
                                Equipment equipment = (Equipment) props.getPropsProperty();
                                // 使用的是 数据库 中的id
                                System.out.println(userEquipmentEntity.getId() + "、类型: " + equipment.getEquipmentType().getType() + " 名称: " + props.getName());
                            }
                        }
                        int nextInt = scanner.nextInt();
                        for (UserEquipmentEntity equipment : userEquipmentEntityList) {
                            if (equipment.getId() == nextInt) {
                                return GameMsg.UserUndoEquipmentCmd.newBuilder().setUserEquipmentId(nextInt).setPropsId(equipment.getPropsId()).build();
                            }
                        }

                    } else if ("12".equals(command)) {

                        Map<Integer, Props> backpackClient = role.getBackpackClient();

                        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
                        UserEquipmentEntity[] userEquipmentEntityArr = role.getUserEquipmentEntityArr();
                        for (int i = 0; i < userEquipmentEntityArr.length; i++) {
                            if (userEquipmentEntityArr[i] != null && propsMap.get(userEquipmentEntityArr[i].getPropsId()).getPropsProperty().getType() == PropsType.Equipment) {
                                Props props = propsMap.get(userEquipmentEntityArr[i].getPropsId());
                                Equipment equipment = (Equipment) props.getPropsProperty();
                                if (equipment.getEquipmentType() == EquipmentType.Weapon) {
                                    System.out.println(userEquipmentEntityArr[i].getId() + "、 名称: " + props.getName() + "、 耐久度 " + userEquipmentEntityArr[i].getDurability());
                                }
                            }
                        }

                        for (Map.Entry<Integer, Props> propsEntry : backpackClient.entrySet()) {
                            if (propsEntry.getValue().getPropsProperty().getType() == PropsType.Equipment) {
                                Props props = propsEntry.getValue();
                                Equipment equipment = (Equipment) props.getPropsProperty();
                                if (equipment.getEquipmentType() == EquipmentType.Weapon) {
                                    System.out.println(equipment.getId() + "、 名称: " + props.getName() + "、 耐久度 " + equipment.getDurability());
                                }

                            }

                        }
                        int nextInt = scanner.nextInt();
                        return GameMsg.RepairEquipmentCmd.newBuilder().setUserEquipmentId(nextInt).build();
                    } else if ("13".equals(command)) {
                        Map<Integer, Duplicate> duplicateMap = GameData.getInstance().getDuplicateMap();
                        for (Duplicate duplicate : duplicateMap.values()) {
                            System.out.println(duplicate.getId() + "、" + duplicate.getName());
                        }
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        return GameMsg.EnterDuplicateCmd.newBuilder().setDuplicateId(id).build();
                    } else if ("14".equals(command)) {
                        Map<Integer, Integer> goodsAllowNumber = role.getGOODS_ALLOW_NUMBER();
                        // 商店, 放在客户端展示商品列表;
                        GameData gameData = GameData.getInstance();
                        Map<Integer, Goods> goodsMap = gameData.getGoodsMap();
                        Map<Integer, Props> propsMap = gameData.getPropsMap();
                        for (Goods goods : goodsMap.values()) {
                            Props props = propsMap.get(goods.getPropsId());
                            if (props.getPropsProperty().getType() != PropsType.Equipment) {
                                System.out.println(goods.getId() + "、" + props.getName() + "  还能购买: " + goodsAllowNumber.get(goods.getId()));

                            } else {
                                System.out.println(goods.getId() + "、" + props.getName() + "  ");
                            }

                        }

                        System.out.println("===>请选择要购买的商品: ");
                        int goodsId = scanner.nextInt();
                        GameMsg.UserBuyGoodsCmd.Builder goodsBuilder = GameMsg.UserBuyGoodsCmd.newBuilder();
                        goodsBuilder.setGoodsId(goodsId);
                        goodsBuilder.setGoodsNumber(1);
                        if (propsMap.get(goodsMap.get(goodsId).getPropsId()).getPropsProperty().getType() == PropsType.Potion) {
                            System.out.println("请输入购买的数量: ");
                            int number = scanner.nextInt();
                            goodsBuilder.setGoodsNumber(number);
                        }

                        GameMsg.UserBuyGoodsCmd userBuyGoodsCmd = goodsBuilder.build();
                        return userBuyGoodsCmd;
                    } else if ("15".equals(command)) {
                        // 聊天
                        System.out.println("1、私聊;");
                        System.out.println("2、群聊;");
                        int cmd = scanner.nextInt();
                        scanner.nextLine();
                        if (cmd == 1) {
                            // 私聊
                            role.setChat(true);
                            return GameMsg.WhoElseIsHereCmd.newBuilder().build();
                        } else if (cmd == 2) {
                            // 群聊
                            role.setSelf(true);
                            System.out.println("请输入信息:");
                            String info = scanner.nextLine();

                            return GameMsg.UserChatInfoCmd.newBuilder()
                                    .setType(ChatType.PUBLIC_CHAT.getChatType())
                                    .setInfo(info)
                                    .build();
                        }
                    } else if ("16".equals(command)) {
                        // 先根据名字找到对应的用户
                        return GameMsg.AllUserCmd.newBuilder().build();
                    } else if ("17".equals(command)) {
                        // 查看邮箱
                        MailClient mail = role.getMail();
                        if (mail.isHave()) {
                            Map<Long, MailEntityClient> mailMap = mail.getMailMap();
                            System.out.println("0、全部领取;");
                            for (MailEntityClient mailEntityClient : mailMap.values()) {
                                System.out.println(mailEntityClient.getId() + "、" + mailEntityClient.getTitle() + "。 来自:" + mailEntityClient.getSrcUserName() + " " + mailEntityClient.getMailType().getState());
                            }
                            System.out.println("999、清理邮件;");
                            System.out.println("===================");
                            long mailId = scanner.nextLong();
                            scanner.nextLine();
                            GameMsg.UserReceiveMailCmd.Builder newBuilder = GameMsg.UserReceiveMailCmd.newBuilder();
                            if (mailId == 0) {
                                newBuilder.setMailId(MailType.RECEIVE_ALL.getState());
                            } else if (mailId == 999) {

                                mailMap.entrySet().removeIf(next -> next.getValue().getMailType() == MailType.READ);

                                mail.setHave(mailMap.size() > 0);
                                return GameMsg.UserCleanMailCmd.newBuilder().build();
                            } else {
                                newBuilder.setMailId(mailId);
                            }
                            return newBuilder.build();
                        } else {
                            System.out.println("没有邮件");
                            continue;
                        }
                    } else if ("18".equals(command)) {
                        // 进入竞技场
                        return GameMsg.UserEnterArenaCmd.newBuilder().build();
                    } else if ("19".equals(command)) {
                        // 组队
                        role.setTeam(true);
                        return GameMsg.WhoElseIsHereCmd.newBuilder().build();
                    } else if ("20".equals(command)) {
                        // 加入队伍
                        role.setAnswer(true);
                        GameMsg.UserJoinTeamCmd userJoinTeamCmd = GameMsg.UserJoinTeamCmd.newBuilder()
                                .setIsJoin(true)
                                .setOriginateUserId(role.getTEAM_CLIENT().getOriginateUserId())
                                .build();
                        return userJoinTeamCmd;
                    } else if ("21".equals(command)) {
                        // 不加队伍
                        role.getTEAM_CLIENT().setOriginateUserId(null);
                        GameMsg.UserJoinTeamCmd userJoinTeamCmd = GameMsg.UserJoinTeamCmd.newBuilder()
                                .setIsJoin(false)
                                .build();
                        return userJoinTeamCmd;
                    } else if ("22".equals(command)) {
                        //  退出队伍
                        return GameMsg.UserQuitTeamCmd.newBuilder()
                                .build();
                    } else if ("23".equals(command)) {
                        // 组队状态, 进入副本线程
                        BossThread.getInstance().process(ctx, role);
                        return null;
                    } else if ("24".equals(command)) {
                        // 交易
                        role.setDeal(true);
                        return GameMsg.WhoElseIsHereCmd.newBuilder().build();
                    } else if ("25".equals(command)) {
                        // 同意交易
                        return GameMsg.DealTargetUserResponseCmd.newBuilder()
                                .setIsAgree(true)
                                .setOriginateId(role.getDEAL_CLIENT().getOriginateId())
                                .build();
                    } else if ("26".equals(command)) {
                        // 拒绝交易
                        role.getDEAL_CLIENT().setOriginateId(null);
                        return GameMsg.DealTargetUserResponseCmd.newBuilder()
                                .setIsAgree(false)
                                .build();
                    } else if ("27".equals(command)) {
                        // 建立交易通道
                        DealThread.getInstance().process(ctx, role);
                        return null;
                    } else if ("28".equals(command)) {
                        //公会操作
                        System.out.println("===>0:退出;");
                        System.out.println("===>1:创建公会,需要200000金币;");
                        System.out.println("===>2:解散公会;");
                        System.out.println("===>3:任命;");
                        System.out.println("===>4:转移会长;");
                        System.out.println("===>5:退出公会;");
                        System.out.println("===>6:踢出公会;");
                        System.out.println("===>7:查看公会仓库;");
                        System.out.println("===>8:查看公会成员;");
                        System.out.println("===>9:申请加入公会,公会列表;");
                        if (role.getPlayGuildClient() != null) {
                            System.out.println("公会名称: " + role.getPlayGuildClient().getGuildName());
                            System.out.println("公会职位: " + role.getPlayGuildClient().getType());
                        }
                        int nextInt = scanner.nextInt();
                        scanner.nextLine();

                        if (nextInt == 1) {
                            System.out.println("请输入公会名称:");
                            String name = scanner.nextLine();

                            return GameMsg.UserCreateGuildCmd.newBuilder()
                                    .setGuildName(name)
                                    .build();
                        } else if (nextInt == 2) {
                            if (!role.getPlayGuildClient().getType().equals(GuildMemberType.President.getRoleName())) {
                                System.out.println("你不是会长;");
                                continue;
                            }
                            return GameMsg.UserDissolveGuildCmd.newBuilder().build();
                        } else if (nextInt == 3) {
                            if (role.getPlayGuildClient().getType().equals(GuildMemberType.President.getRoleName())
                                    || role.getPlayGuildClient().getType().equals(GuildMemberType.VicePresident.getRoleName())) {
                                role.setAppoint(true);
                                return GameMsg.ShowGuildMemberCmd.newBuilder().build();
                            }
                        } else if (nextInt == 4) {
                            role.setTransferPresident(true);
                            return GameMsg.ShowGuildMemberCmd.newBuilder().build();
                        } else if (nextInt == 5) {
                            return GameMsg.QuitGuildCmd.newBuilder().build();
                        } else if (nextInt == 6) {
                            if (role.getPlayGuildClient().getType().equals(GuildMemberType.President.getRoleName())
                                    || role.getPlayGuildClient().getType().equals(GuildMemberType.VicePresident.getRoleName())) {
                                role.setEliminate(true);
                                return GameMsg.ShowGuildMemberCmd.newBuilder().build();
                            }
                        } else if (nextInt == 7) {
                            return GameMsg.LookGuildWarehouseCmd.newBuilder().build();
                        } else if (nextInt == 8) {
                            return GameMsg.ShowGuildMemberCmd.newBuilder().build();
                        } else if (nextInt == 9) {
                            return GameMsg.ShowGuildCmd.newBuilder().build();
                        }

                    } else if ("29".equals(command)) {
                        //拍卖行
                        System.out.println("0、退出;");
                        System.out.println("1、上架物品;");
                        System.out.println("2、查看物品;");
                        System.out.println("3、查看上架物品;");
                        int nextInt = scanner.nextInt();
                        scanner.nextLine();

                        if (0 == nextInt) {

                        } else if (1 == nextInt) {
                            //上架商品
                            //背包中的商品
                            Map<Integer, Props> backpackClient = role.getBackpackClient();
                            System.out.println("背包空间: " + backpackClient.size() + "/100");
                            System.out.println("道具如下:");
                            System.out.println("金币: " + role.getMoney());
                            for (Map.Entry<Integer, Props> propsEntry : backpackClient.entrySet()) {
                                if (propsEntry.getValue().getPropsProperty().getType() == PropsType.Equipment) {
                                    System.out.println("==> " + propsEntry.getKey() + "、" + propsEntry.getValue().getName() + "\t\t类型: " + propsEntry.getValue().getPropsProperty().getType().getType());
                                } else if (propsEntry.getValue().getPropsProperty().getType() == PropsType.Potion) {
                                    Potion potion = (Potion) propsEntry.getValue().getPropsProperty();
                                    System.out.println("==> " + propsEntry.getKey() + "、" + propsEntry.getValue().getName() + "\t\t类型: " + propsEntry.getValue().getPropsProperty().getType().getType() + " \t\t数量: " + potion.getNumber());
                                }
                            }

                            //
                            int location = scanner.nextInt();
                            scanner.nextLine();
                            int number = 1;
                            Props props = backpackClient.get(location);
                            if (props.getPropsProperty().getType() == PropsType.Potion) {
                                System.out.println("输入数量:");
                                number = scanner.nextInt();
                                scanner.nextLine();
                            }

                            System.out.println("输入竞拍价:");
                            int auction = scanner.nextInt();
                            scanner.nextLine();
                            System.out.println("输入一口价:");
                            int price = scanner.nextInt();
                            scanner.nextLine();

                            ctx.writeAndFlush(GameMsg.ShelveGoodsCmd.newBuilder()
                                    .setLocation(location)
                                    .setNumber(number)
                                    .setAuction(auction)
                                    .setPrice(price)
                                    .build());
                            if (props.getPropsProperty().getType() == PropsType.Potion) {
                                Potion potion = (Potion) props.getPropsProperty();
                                if (potion.getNumber() > number) {
                                    potion.setNumber(potion.getNumber() - number);
                                } else {
                                    backpackClient.remove(location);
                                }
                            } else {
                                backpackClient.remove(location);
                            }
                        } else if (2 == nextInt) {
                            //查看商品
                            return GameMsg.LookAuctionItemCmd.newBuilder().build();
                        } else if (3 == nextInt) {
                            return GameMsg.LookOneselfAuctionItemCmd.newBuilder().build();
                        }

                    } else if ("30".equals(command)) {
                        //查看任务
                        return GameMsg.LookAllTaskCmd.newBuilder().build();
                    } else {
                        log.error("操作选择错误,请重新输入!");
                        continue;
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }
    }


    /**
     * 发送命令到服务的
     *
     * @param ctx
     * @param cmd 客户端指令
     */
    public static void sendCmd(ChannelHandlerContext ctx, Object cmd) {
        if (cmd instanceof Integer) {
            // 移动，场景切换
            Integer sceneId = (Integer) cmd;
            Role.getInstance().setCurrSceneId(sceneId);
            GameMsg.UserSwitchSceneCmd userSwitchSceneCmd = GameMsg.UserSwitchSceneCmd.newBuilder()
                    .setToSceneId(sceneId)
                    .build();
            ctx.channel().writeAndFlush(userSwitchSceneCmd);
        } else if (cmd instanceof GameMsg.WhoElseIsHereCmd) {
            ctx.channel().writeAndFlush((GameMsg.WhoElseIsHereCmd) cmd);
        } else if (cmd instanceof GameMsg.AttkCmd.Builder) {
            GameMsg.AttkCmd.Builder cmdBuilder = (GameMsg.AttkCmd.Builder) cmd;
            GameMsg.AttkCmd attkCmd = cmdBuilder.build();
            ctx.channel().writeAndFlush(attkCmd);
        } else if (cmd instanceof GameMsg.MonsterStartAttkUser.Builder) {
            GameMsg.MonsterStartAttkUser.Builder builder = (GameMsg.MonsterStartAttkUser.Builder) cmd;
            GameMsg.MonsterStartAttkUser monsterStartAttkUser = builder.build();
            ctx.channel().writeAndFlush(monsterStartAttkUser);
        } else if (cmd instanceof GameMsg.UserSkillAttkCmd) {
            GameMsg.UserSkillAttkCmd userSkillAttkCmd = (GameMsg.UserSkillAttkCmd) cmd;
            ctx.channel().writeAndFlush(userSkillAttkCmd);
        } else if (cmd instanceof GameMsg.BackpackCmd) {
            ctx.channel().writeAndFlush((GameMsg.BackpackCmd) cmd);
        } else if (cmd instanceof GameMsg.UserPotionCmd) {
            ctx.channel().writeAndFlush((GameMsg.UserPotionCmd) cmd);
        } else if (cmd instanceof GameMsg.UserWearEquipmentCmd) {
            ctx.channel().writeAndFlush((GameMsg.UserWearEquipmentCmd) cmd);
        } else if (cmd instanceof GameMsg.UserUndoEquipmentCmd) {
            ctx.channel().writeAndFlush((GameMsg.UserUndoEquipmentCmd) cmd);
        } else if (cmd instanceof GameMsg.RepairEquipmentCmd) {
            ctx.channel().writeAndFlush((GameMsg.RepairEquipmentCmd) cmd);
        } else if (cmd instanceof GameMsg.EnterDuplicateCmd) {
            ctx.channel().writeAndFlush((GameMsg.EnterDuplicateCmd) cmd);
        } else if (cmd instanceof GameMsg.UserBuyGoodsCmd) {
            ctx.channel().writeAndFlush((GameMsg.UserBuyGoodsCmd) cmd);
        } else if (cmd instanceof GameMsg.UserChatInfoCmd) {
            ctx.writeAndFlush((GameMsg.UserChatInfoCmd) cmd);
        } else if (cmd instanceof GameMsg.AllUserCmd) {
            ctx.writeAndFlush((GameMsg.AllUserCmd) cmd);
        } else if (cmd instanceof GameMsg.UserSeeMailCmd) {
            ctx.writeAndFlush((GameMsg.UserSeeMailCmd) cmd);
        } else if (cmd instanceof GameMsg.UserReceiveMailCmd) {
            ctx.writeAndFlush((GameMsg.UserReceiveMailCmd) cmd);
        } else if (cmd instanceof GameMsg.UserEnterArenaCmd) {
            ctx.writeAndFlush((GameMsg.UserEnterArenaCmd) cmd);
        } else if (cmd instanceof GameMsg.UserJoinTeamCmd) {
            ctx.writeAndFlush((GameMsg.UserJoinTeamCmd) cmd);
        } else if (cmd instanceof GameMsg.UserQuitTeamCmd) {
            ctx.writeAndFlush((GameMsg.UserQuitTeamCmd) cmd);
        } else if (cmd instanceof GameMsg.DealTargetUserResponseCmd) {
            ctx.writeAndFlush((GameMsg.DealTargetUserResponseCmd) cmd);
        } else if (cmd instanceof GameMsg.DealChannelCmd) {
            ctx.writeAndFlush((GameMsg.DealChannelCmd) cmd);
        } else if (cmd instanceof GameMsg.UserCreateGuildCmd) {
            ctx.writeAndFlush((GameMsg.UserCreateGuildCmd) cmd);
        } else if (cmd instanceof GameMsg.UserDissolveGuildCmd) {
            ctx.writeAndFlush((GameMsg.UserDissolveGuildCmd) cmd);
        } else if (cmd instanceof GameMsg.ShowGuildCmd) {
            ctx.writeAndFlush((GameMsg.ShowGuildCmd) cmd);
        } else if (cmd instanceof GameMsg.ShowGuildMemberCmd) {
            ctx.writeAndFlush((GameMsg.ShowGuildMemberCmd) cmd);
        } else if (cmd instanceof GameMsg.QuitGuildCmd) {
            ctx.writeAndFlush((GameMsg.QuitGuildCmd) cmd);
        } else if (cmd instanceof GameMsg.LookGuildWarehouseCmd) {
            ctx.writeAndFlush((GameMsg.LookGuildWarehouseCmd) cmd);
        } else if (cmd instanceof GameMsg.LookAuctionItemCmd) {
            ctx.writeAndFlush((GameMsg.LookAuctionItemCmd) cmd);
        } else if (cmd instanceof GameMsg.UserCleanMailCmd) {
            ctx.writeAndFlush((GameMsg.UserCleanMailCmd) cmd);
        } else if (cmd instanceof GameMsg.LookOneselfAuctionItemCmd) {
            ctx.writeAndFlush((GameMsg.LookOneselfAuctionItemCmd) cmd);
        }else if (cmd instanceof GameMsg.LookAllTaskCmd){
            ctx.writeAndFlush((GameMsg.LookAllTaskCmd)cmd);
        }else if (cmd instanceof  GameMsg.DialogueTaskCmd){
            ctx.writeAndFlush((GameMsg.DialogueTaskCmd)cmd);
        }


    }


}
