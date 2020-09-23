package client.cmd;

import client.model.server.props.AbstractPropsProperty;
import client.model.server.props.Equipment;
import client.model.server.props.Potion;
import client.model.server.props.Props;
import client.model.server.scene.Monster;
import client.model.server.scene.Scene;
import client.model.Role;
import client.model.SceneData;
import constant.EquipmentConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import msg.GameMsg;
import type.PropsType;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 */
@Slf4j
public class DieResultClient implements ICmd<GameMsg.DieResult> {

    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.DieResult dieResult) {
        if (ctx == null || dieResult == null) {
            return;
        }

        Role role = Role.getInstance();
        Scene scene = SceneData.getInstance().getSceneMap().get(role.getCurrSceneId());

        // 重新设置恢复mp终止时间
        role.getUserResumeState().setEndTimeMp(dieResult.getResumeMpEndTime());
        role.startResumeMp();

        if (dieResult.getTargetUserId() == role.getId()) {
            // 用户阵亡
            role.setShieldValue(0);
            role.setCurrHp(0);
            // 清空正在攻击自己的怪
            GameMsg.StopCurUserAllTimer stopCurUserAllTimer = GameMsg.StopCurUserAllTimer.newBuilder().build();
            ctx.channel().writeAndFlush(stopCurUserAllTimer);

        } else if (dieResult.getMonsterId() != 0) {
            // 怪被击杀  怪id
            int monsterId = dieResult.getMonsterId();
            Map<Integer, Monster> monsterMap = scene.getMonsterMap();
            // 已被击杀的怪
            // 设置状态， 已死
            Monster monster = monsterMap.get(dieResult.getMonsterId());
            if (!dieResult.getIsDieBefore()) {
                monster.setHp(0);
                System.out.println(monster.getName() + "被击杀!");

                Map<Integer, Props> propsMap = SceneData.getInstance().getPropsMap();

                List<GameMsg.Props> propsList = dieResult.getPropsList();
                for (GameMsg.Props props : propsList) {
                    int propsId = props.getPropsId();
                    long userPropsId = props.getUserPropsId();
                    int propsNumber = 1;
                    int location = props.getLocation();

                    Props p = role.getBackpackClient().get(location);
                    if (p == null) {
                        //添加
                        Props addP = propsMap.get(propsId);
                        System.out.println("获得道具: " + addP.getName());


                        if (addP.getPropsProperty().getType() == PropsType.Equipment) {
                            Equipment eq = (Equipment) addP.getPropsProperty();

                            Equipment propsProperty =
                                    //props.getUserPropsId() 是 表 user_equipment 中的id
                                    new Equipment(userPropsId,
                                            eq.getPropsId(),
                                            props.getDurability(),
                                            eq.getDamage(),
                                            eq.getEquipmentType());

                            role.getBackpackClient().put(location, new Props(propsId, addP.getName(), propsProperty));

                        } else if (addP.getPropsProperty().getType() == PropsType.Potion) {
                            Potion po = (Potion) addP.getPropsProperty();
                            Potion propsProperty =
                                    new Potion(props.getUserPropsId(),
                                            po.getPropsId(),
                                            po.getCdTime(),
                                            po.getInfo(),
                                            po.getResumeFigure(),
                                            po.getPercent(),
                                            propsNumber);

                            role.getBackpackClient().put(location, new Props(propsId, addP.getName(), propsProperty));
                        }

                    } else {
                        //道具
                        ((Potion) p.getPropsProperty()).setNumber(((Potion) p.getPropsProperty()).getNumber() + propsNumber);
                        System.out.println("获得道具: " + p.getName());
                    }
                }

            } else {
                System.out.println(monster.getName() + " 已被击杀!");
            }
        }
//        CmdThread.getInstance().process(ctx, Role.getInstance(), scene.getNpcMap().values());
    }


}
