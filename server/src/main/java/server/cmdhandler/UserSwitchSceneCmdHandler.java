package server.cmdhandler;

import constant.SceneConst;
import entity.db.CurrUserStateEntity;
import entity.db.UserEquipmentEntity;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import server.model.props.Equipment;
import server.model.props.Props;
import server.model.scene.Monster;
import server.model.scene.Npc;
import server.model.scene.Scene;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.scene.GameData;
import server.Broadcast;
import server.PublicMethod;
import server.model.User;
import server.model.UserManager;
import server.timer.state.DbUserStateTimer;
import type.EquipmentType;
import type.SceneType;
import util.MyUtil;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 * 切换场景 处理类
 */
@Slf4j
@Component
public class UserSwitchSceneCmdHandler implements ICmdHandler<GameMsg.UserSwitchSceneCmd> {

    @Autowired
    private DbUserStateTimer userStateTimer;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserSwitchSceneCmd cmd) {

        MyUtil.checkIsNull(ctx, cmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        GameMsg.UserSwitchSceneResult.Builder resultBuilder = GameMsg.UserSwitchSceneResult.newBuilder();

        // 用户目标场景
        Scene toScene = GameData.getInstance().getSceneMap().get(cmd.getToSceneId());
        log.info("用户当前场景: {}", toScene.getName());

        PublicMethod.getInstance().cancelMonsterAttack(user);

        // 封装目标场景的npc
        if (toScene.getNpcMap().size() != 0) {
            for (Npc npc : toScene.getNpcMap().values()) {
                GameMsg.UserSwitchSceneResult.Npc.Builder npcBuilder = GameMsg.UserSwitchSceneResult.Npc.newBuilder()
                        .setName(npc.getName())
                        .setSceneId(npc.getSceneId())
                        .setInfo(npc.getInfo())
                        .setId(npc.getId());
                resultBuilder.addNpc(npcBuilder);
            }
        }

        // 封装当前场景中的 怪
        if (toScene.getMonsterMap().size() != 0) {
            for (Monster monster : toScene.getMonsterMap().values()) {
                GameMsg.UserSwitchSceneResult.MonsterInfo.Builder monsterBuilder = GameMsg.UserSwitchSceneResult.MonsterInfo.newBuilder()
                        .setName(monster.getName())
                        .setIsDie(monster.isDie())
                        .setHp(monster.getHp())
                        .setMonsterId(monster.getId());

                resultBuilder.addMonsterInfo(monsterBuilder);
            }
        }

        // 把对应的 channel 加入到ChannelGroup中
        // 把信道转移到要移动到的场景中
        Broadcast.removeChannel(user.getCurSceneId(), ctx.channel());
        Broadcast.addChannel(toScene.getId(), ctx.channel());


        //如果当前场景是公共地图，切换地图时则更新装备耐久度
        List<Integer> sceneId = SceneType.getSceneIdByType("野外");
        if (sceneId.contains(user.getCurSceneId())){
            //持久化装备耐久度
            Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
            for (UserEquipmentEntity equipmentEntity : user.getUserEquipmentArr()) {
                if (equipmentEntity != null) {
                    if (((Equipment) propsMap.get(equipmentEntity.getPropsId()).getPropsProperty()).getEquipmentType() == EquipmentType.Weapon) {
                        //如果是武器
                        userStateTimer.modifyUserEquipment(equipmentEntity);
                    }
                }

            }
        }


        // 修改 当前用户所在场景
        user.setCurSceneId(cmd.getToSceneId());
        GameMsg.UserSwitchSceneResult userSwitchSceneResult = resultBuilder.build();
        ctx.channel().writeAndFlush(userSwitchSceneResult);
    }


}
