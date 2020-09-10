package client.cmd;

import client.model.server.props.Props;
import client.model.server.scene.Monster;
import client.model.server.scene.Scene;
import client.thread.CmdThread;
import client.model.Role;
import client.model.SceneData;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

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
                Props props = propsMap.get(dieResult.getPropsId());
                System.out.println("获得道具: " + props.getName());

            } else {
                System.out.println(monster.getName() + " 已被击杀!");
            }
        }
//        CmdThread.getInstance().process(ctx, Role.getInstance(), scene.getNpcMap().values());
    }



}
