package client.cmd.guild;

import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import client.model.server.props.Props;
import client.scene.GameData;
import client.thread.CmdThread;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

import java.util.List;

/**
 * @author 张丰博
 */
public class LookGuildMemberInfoResultClient implements ICmd<GameMsg.LookGuildMemberInfoResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.LookGuildMemberInfoResult lookGuildMemberInfoResult) {

        MyUtil.checkIsNull(ctx, lookGuildMemberInfoResult);
        Role role = Role.getInstance();

        List<Integer> equIdList = lookGuildMemberInfoResult.getEquIdList();
        System.out.println("穿戴装备如下:");
        for (Integer equId : equIdList) {
            Props props = GameData.getInstance().getPropsMap().get(equId);
            System.out.println(props.getName());
        }
        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
    }
}
