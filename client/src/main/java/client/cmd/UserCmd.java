package client.cmd;

import client.model.Role;
import entity.db.UserEquipmentEntity;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import model.profession.Skill;
import model.props.Equipment;
import model.props.Potion;
import model.props.Props;
import model.scene.Npc;
import model.scene.Scene;
import msg.GameMsg;
import scene.GameData;
import type.EquipmentType;
import type.PropsType;

import java.util.Collection;
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
                log.info("============当前场景的npc个数: {}", (npcList == null ? 0 : npcList.size()));
                if (npcList != null) {
                    for (Npc npc : npcList) {
                        log.info("===>> npc 名称: {}", npc.getName());
                    }
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
                        return null;
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
                            Npc npc = scene.getNpcMap().get(npcId);
                            System.out.println(npc.getName() + ": " + npc.getInfo());
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
                        for (Props props : backpackClient.values()) {
                            System.out.println("==> " + props.getId() + "、" + props.getName() + "\t\t类型: " + props.getPropsProperty().getType().getType());
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
                        return GameMsg.DuplicateCmd.newBuilder().build();
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

        } else if (cmd == null) {

            GameMsg.WhoElseIsHereCmd isHereCmd = GameMsg.WhoElseIsHereCmd.newBuilder().build();
            ctx.channel().writeAndFlush(isHereCmd);

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
        }else if (cmd instanceof GameMsg.DuplicateCmd){
            ctx.channel().writeAndFlush((GameMsg.DuplicateCmd)cmd);
        }


    }


}
