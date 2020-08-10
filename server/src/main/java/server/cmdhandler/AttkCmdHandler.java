package server.cmdhandler;

import constant.SceneConst;
import entity.db.UserEquipmentEntity;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import model.props.Equipment;
import model.props.Props;
import model.scene.Monster;
import model.scene.Scene;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scene.GameData;
import server.Broadcast;
import server.model.User;
import server.model.UserManager;
import server.service.UserService;
import type.EquipmentType;
import type.PropsType;

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

        if (ctx == null || cmd == null) {
            return;
        }

        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        User user = UserManager.getUserById(userId);

        Scene curScene = GameData.getInstance().getSceneMap().get(user.getCurSceneId());

        // 当前场景的 怪
        Map<Integer, Monster> monsterMap = curScene.getMonsterMap();
        GameMsg.AttkResult.Builder attkResultBuilder = GameMsg.AttkResult.newBuilder();
        if (monsterMap.size() != 0) {
            //存活的 怪
            List<Monster> monsterAliveList = getMonsterAliveList(monsterMap.values());
            if (monsterAliveList.size() != 0) {
                // 普通攻击
                // 随机选中一个怪
                Monster monster = monsterAliveList.remove((int) (Math.random() * monsterAliveList.size()));
                int subHp = user.calMonsterSubHp(null);
                //持久化装备耐久度
                UserEquipmentEntity[] userEquipmentArr = user.getUserEquipmentArr();
                Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
                for (int i = 0; i < userEquipmentArr.length; i++) {
                    if (userEquipmentArr[i] != null
                            && ((Equipment)propsMap.get(userEquipmentArr[i].getPropsId()).getPropsProperty()).getEquipmentType()==EquipmentType.Weapon){
                        userService.modifyEquipmentDurability(userEquipmentArr[i].getId(),userEquipmentArr[i].getDurability());
                    }
                }


                // 使用当前被攻击的怪对象，做锁对象
                synchronized (monster.getSubHpMontor()) {
                    // 减血  (0~99) + 500
                    if (monster.isDie()) {
                        log.info("{} 已被其他玩家击杀!", monster.getName());
                        // 有可能刚被前一用户杀死，
                        GameMsg.DieResult dieResult = GameMsg.DieResult.newBuilder()
                                .setMonsterId(monster.getId())
                                .setIsDieBefore(true)
                                .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                                .build();
                        ctx.channel().writeAndFlush(dieResult);
                        return;
                    } else if (monster.getHp() <= subHp) {
                        log.info("玩家:{},击杀:{}!", user.getUserName(), monster.getName());
                        // 死亡
                        monster.setHp(0);
                        // 爆道具
                        GameMsg.DieResult.Builder dieBuilder = GameMsg.DieResult.newBuilder();

                        String propsIdString = monster.getPropsId();
                        String[] split = propsIdString.split(",");
                        int propsId = Integer.parseInt(split[(int) Math.random()*split.length]);
                        //持久化数据库

                        Props props = GameData.getInstance().getPropsMap().get(propsId);
                        if (props.getPropsProperty().getType() == PropsType.Equipment){
//                            Equipment equipment = (Equipment) props.getPropsProperty();
//                            UserEquipmentEntity userEquipmentEntity = new UserEquipmentEntity();
//                            userEquipmentEntity.setIsWear(0);
//                            userEquipmentEntity.setDurability(equipment.getDurability());
//                            userEquipmentEntity.setLocation(-1);
//                            userEquipmentEntity.setPropsId(propsId);
//                            userEquipmentEntity.setUserId(userId);
//
//                            userService.addEquipment(userEquipmentEntity);
                        }else if (props.getPropsProperty().getType() == PropsType.Potion){
//                            Potion potion = (Potion) props.getPropsProperty();
//                            UserPotionEntity userPotionEntity = new UserPotionEntity();
//                            userPotionEntity.setLocation(-1);
//
//
//                            userService.addPotion();
                        }

                        GameMsg.DieResult dieResult = dieBuilder.setMonsterId(monster.getId())
                                .setIsDieBefore(false)
                                .setPropsId(propsId)
                                .setResumeMpEndTime(user.getUserResumeState().getEndTimeMp())
                                .build();

                        Broadcast.broadcast(user.getCurSceneId(), dieResult);
                        return;
                    } else {
                        // 减血
                        monster.setHp(monster.getHp() - subHp);
                        log.info("玩家:{},使:{} 减血 {}!", user.getUserName(), monster.getName(), subHp);
                    }
                }

                // 通知客户端 xx怪 减血；
                attkResultBuilder.setMonsterId(monster.getId())
                        .setSubHp(subHp);
                // 怪减血，广播通知当前场景所有用户
                GameMsg.AttkResult attkResult = attkResultBuilder.build();

                Broadcast.broadcast(user.getCurSceneId(), attkResult);
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
