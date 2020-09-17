package client.cmd.duplicate;

import client.model.server.scene.Scene;
import client.CmdThread;
import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import constant.ProfessionConst;
import io.netty.channel.ChannelHandlerContext;
import msg.GameMsg;
import util.MyUtil;

/**
 * @author 张丰博
 */
public class UserQuitDuplicateResultClient implements ICmd<GameMsg.UserQuitDuplicateResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserQuitDuplicateResult userQuitDuplicateResult) {

        MyUtil.checkIsNull(ctx, userQuitDuplicateResult);

        // 用户退出类型
        String quitDuplicateType = userQuitDuplicateResult.getQuitDuplicateType();
        Role role = Role.getInstance();

        role.setCurrDuplicate(null);

        role.setCurrHp(ProfessionConst.HP);
        synchronized (role.getMpMonitor()){
            role.setCurrMp(ProfessionConst.MP);
        }

        Scene scene = SceneData.getInstance().getSceneMap().get(role.getCurrSceneId());
        System.out.println("退出副本");
        CmdThread.getInstance().process(ctx, role, scene.getNpcMap().values());
    }
}
