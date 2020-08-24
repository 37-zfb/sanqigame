package server.cmdhandler;

import constant.SceneConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import server.model.scene.Monster;
import server.model.scene.Scene;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.scene.GameData;
import server.PublicMethod;
import server.model.User;
import server.service.UserService;
import util.MyUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 玩家普通攻击 怪
 * @author 张丰博
 */
@Slf4j
@Component
public class AttkCmdHandler implements ICmdHandler<GameMsg.AttkCmd> {

    @Autowired
    private UserService userService;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.AttkCmd cmd) {
        MyUtil.checkIsNull(ctx,cmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        Scene curScene = GameData.getInstance().getSceneMap().get(user.getCurSceneId());
        // 当前场景的 怪
        Map<Integer, Monster> monsterMap = curScene.getMonsterMap();
        GameMsg.AttkResult.Builder attkResultBuilder = GameMsg.AttkResult.newBuilder();
        if (monsterMap.size() != 0) {
            //存活的 怪
            List<Monster> monsterAliveList = getMonsterAliveList(monsterMap.values());
            if (monsterAliveList.size() != 0) {
                Monster monster = monsterAliveList.remove((int) (Math.random() * monsterAliveList.size()));
                PublicMethod.getInstance().userOrSummonerAttackMonster(user,monster,null,user.calMonsterSubHp());
                return;
            } else {
                // -1 表示当前场景的怪被0存活
                attkResultBuilder.setSubHp(-1);
            }

        } else {
            System.out.println(curScene.getName() + " 没有怪!");
            //  减血=int最小值 , 表示当前场景没有怪；
            attkResultBuilder.setSubHp(SceneConst.NO_MONSTER);
        }

        GameMsg.AttkResult attkResult = attkResultBuilder.build();
        ctx.channel().writeAndFlush(attkResult);
    }


    /**
     * 返回还存活的怪集合
     *
     * @param monsterList 所有怪的集合
     * @return 存活怪的集合
     */
    private List<Monster> getMonsterAliveList(Collection<Monster> monsterList) {
        List<Monster> monsterAliveList = new ArrayList<>();
        //存活的 怪
        for (Monster monster : monsterList) {
            if (!monster.isDie()) {
                monsterAliveList.add(monster);
            }
        }
        return monsterAliveList;
    }


}
