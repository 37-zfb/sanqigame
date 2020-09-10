package server.cmdhandler;

import constant.SceneConst;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
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
import server.timer.MonsterTimer;
import util.MyUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 玩家普通攻击 怪
 *
 * @author 张丰博
 */
@Slf4j
@Component
public class AttkCmdHandler implements ICmdHandler<GameMsg.AttkCmd> {

    @Autowired
    private UserService userService;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.AttkCmd cmd) {
        MyUtil.checkIsNull(ctx, cmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        Scene curScene = GameData.getInstance().getSceneMap().get(user.getCurSceneId());
        Map<Integer, Monster> monsterMap = curScene.getMonsterMap();
        if (monsterMap.size() == 0) {
            throw new CustomizeException(CustomizeErrorCode.SCENE_NOT_MONSTER);
        }


        List<Monster> monsterAliveList = PublicMethod.getInstance().getMonsterAliveList(monsterMap.values());
        if (monsterAliveList.size() == 0) {
            throw new CustomizeException(CustomizeErrorCode.ALL_MONSTER_DIE);
        }


        Monster monster = monsterAliveList.remove((int) (Math.random() * monsterAliveList.size()));
        PublicMethod.getInstance().userOrSummonerAttackMonster(user, monster, null, user.calMonsterSubHp());

        monsterAliveList = PublicMethod.getInstance().getMonsterAliveList(monsterMap.values());
        if (monsterAliveList.size() == 0) {
            MonsterTimer.getInstance().resurrectionMonster(monsterMap.values(),user.getCurSceneId());
        }


//        GameMsg.AttkResult.Builder attkResultBuilder = GameMsg.AttkResult.newBuilder();
//        GameMsg.AttkResult attkResult = attkResultBuilder.build();
//        ctx.channel().writeAndFlush(attkResult);
    }


}
