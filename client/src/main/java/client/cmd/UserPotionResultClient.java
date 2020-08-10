package client.cmd;

import client.CmdThread;
import client.model.Role;
import client.model.SceneData;
import constant.ProfessionConst;
import io.netty.channel.ChannelHandlerContext;
import model.props.Props;
import msg.GameMsg;
import model.props.Potion;
import type.PotionType;

import java.util.Map;

/**
 * @author 张丰博
 */
public class UserPotionResultClient implements ICmd<GameMsg.UserPotionResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserPotionResult userPotionResult) {
        if (ctx == null || userPotionResult == null) {
            return;
        }

        int location = userPotionResult.getLocation();

        Role role = Role.getInstance();
        if (userPotionResult.getIsSuccess()) {
            Map<Integer, Props> backpackClient = role.getBackpackClient();
            Props props = backpackClient.get(location);

            Potion potion = (Potion) props.getPropsProperty();
            // 数量-1
            potion.setNumber(potion.getNumber()-1);
            if (potion.getInfo().contains(PotionType.HP.getType())) {
                // 恢复HP
                if (potion.getInfo().contains(PotionType.IMMEDIATELY.getType())) {
                    // 立即恢复HP
                    int addHP = (int) (ProfessionConst.HP * potion.getPercent() + potion.getResumeFigure());
                    System.out.println("使用药剂前,玩家HP: " + role.getCurrHp());
                    synchronized (role.getHpMonitor()) {
                        if ((role.getCurrHp() + addHP) >= ProfessionConst.HP) {
                            role.setCurrHp(ProfessionConst.HP);
                            System.out.println("使用药剂后,玩家HP: " + role.getCurrHp());
                        } else {
                            role.setCurrHp(role.getCurrHp() + addHP);
                        }
                    }

                } else if (potion.getInfo().contains(PotionType.SLOW.getType())) {
                    // 缓慢恢复mp
                    potion.setUsedEndTime(userPotionResult.getResumeHpEndTime());
                    //启动定时器
                    role.startPotionResumeState(potion);

                }

            } else if (potion.getInfo().contains(PotionType.MP.getType())) {
                // 恢复MP
                if (potion.getInfo().contains(PotionType.IMMEDIATELY.getType())) {
                    // 立即恢复MP
                    // 设置终止时间
                    role.getUserResumeState().setEndTimeMp(userPotionResult.getResumeMpEndTime());

                    int addMP = (int) (ProfessionConst.MP * potion.getPercent() + potion.getResumeFigure());
                    synchronized (role.getMpMonitor()) {
                        if ((role.getCurrMp() + addMP) >= ProfessionConst.MP) {
                            // 已满
                            role.setCurrMp(ProfessionConst.MP);
                            System.out.println("玩家当前MP: " + role.getCurrMp());
                            role.setMpTask(null);
                        } else {
                            // 未满
                            role.setCurrMp(role.getCurrMp() + addMP);
                            role.startResumeMp();
                        }
                    }
                } else if (potion.getInfo().contains(PotionType.SLOW.getType())) {
                    // 缓慢恢复mp
                    // 缓慢恢复mp
                    potion.setUsedEndTime(userPotionResult.getResumeMpEndTime());
                    role.getUserResumeState().setEndTimeMp(userPotionResult.getResumeMpEndTimeAuto());
                    //启动定时器
                    role.startResumeMp();
                    role.startPotionResumeState(potion);
                }
            }

            potion.setLastTimeSkillTime(System.currentTimeMillis());
        } else {
            System.out.println("冷却中;");
        }
        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
    }
}
