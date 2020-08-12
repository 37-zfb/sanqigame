package client.cmd;

import client.CmdThread;
import client.model.Role;
import client.model.SceneData;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import model.props.Props;
import model.scene.Monster;
import model.scene.Scene;
import msg.GameMsg;

import java.util.Map;
import java.util.Scanner;

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
            if (role.getCurrDuplicate() != null){
                // 用户在副本中阵亡,清空副本
                System.out.println("您已阵亡,副本: "+role.getCurrDuplicate().getName()+" ,Boss: "+role.getCurrDuplicate().getCurrBossMonster().getBossName());

                GameMsg.UserQuitDuplicateCmd build = GameMsg.UserQuitDuplicateCmd.newBuilder().build();
                ctx.writeAndFlush(build);
            }else {
                // 清空正在攻击自己的怪
                GameMsg.StopCurUserAllTimer stopCurUserAllTimer = GameMsg.StopCurUserAllTimer.newBuilder().build();
                ctx.channel().writeAndFlush(stopCurUserAllTimer);
            }
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
                Props props = propsMap.get(dieResult.getPropsId());
                System.out.println("获得道具: " + props.getName());

            } else {
                System.out.println(monster.getName() + " 已被击杀!");
            }
        }
        CmdThread.getInstance().process(ctx, Role.getInstance(), scene.getNpcMap().values());
    }

    /**
     * 抢装备or放弃
     */
    private void robOrGiveUpEqu(ChannelHandlerContext ctx, Monster monster, Integer type) {
        while (true) {
            System.out.println("1、捡起;");
            System.out.println("2、放弃;");
            Scanner scanner = new Scanner(System.in);
            String cmd = scanner.nextLine();
            if ("2".equals(cmd)) {
                //放弃
                log.info("放弃当前装备!");
                break;
            } else if ("1".equals(cmd)) {
                // 捡起
                log.info("抢道具!");
                GameMsg.RobEquipmentCmd robPropsCmd = GameMsg.RobEquipmentCmd.newBuilder().setMonsterId(monster.getId()).setType(type).build();
                ctx.channel().writeAndFlush(robPropsCmd);
                break;
            } else {
                System.out.println("命令有误请重新输入!");
            }
        }


    }


}
