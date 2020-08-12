package client.cmd.duplicate;

import client.BossThread;
import client.CmdThread;
import client.cmd.ICmd;
import client.model.Role;
import client.model.SceneData;
import io.netty.channel.ChannelHandlerContext;
import model.scene.Scene;
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
        Scene scene = SceneData.getInstance().getSceneMap().get(role.getCurrSceneId());

        System.out.println(quitDuplicateType);
        CmdThread.getInstance().process(ctx, role, scene.getNpcMap().values());
    }
}
