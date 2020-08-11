package server.cmdhandler.duplicate;

import constant.BackPackConst;
import constant.DuplicateConst;
import constant.EquipmentConst;
import entity.db.UserEquipmentEntity;
import entity.db.UserPotionEntity;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import model.duplicate.BossMonster;
import model.duplicate.Duplicate;
import model.props.Equipment;
import model.props.Potion;
import model.props.Props;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scene.GameData;
import server.cmdhandler.ICmdHandler;
import server.model.User;
import server.model.UserManager;
import server.service.UserService;
import type.DuplicateType;
import type.PropsType;
import util.MyUtil;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 */
@Component
@Slf4j
public class AttkBossCmdHandler implements ICmdHandler<GameMsg.AttkBossCmd> {
    @Autowired
    private UserService userService;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.AttkBossCmd attkBossCmd) {
        MyUtil.checkIsNull(ctx, attkBossCmd);

        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        User user = UserManager.getUserById(userId);

        Duplicate currDuplicate = user.getCurrDuplicate();

        Integer subHp = user.calMonsterSubHp();

        BossMonster currBossMonster = currDuplicate.getCurrBossMonster();
        if (currBossMonster.getHp() < subHp) {
            // 剩余血量 小于 应减少的值 boss已死
            //
            if (currDuplicate.getBossMonsterMap().size() > 0) {
                // 此时副本中还存在boss
                currDuplicate.setMinBoss();
                GameMsg.NextBossResult nextBossResult = GameMsg.NextBossResult.newBuilder()
                        .setBossMonsterId(currDuplicate.getCurrBossMonster().getId())
                        .setStartTime(System.currentTimeMillis() + DuplicateConst.INIT_TIME)
                        .build();
                ctx.writeAndFlush(nextBossResult);

            } else {
                // 此时副本已通关，计算奖励，退出副本
                System.out.println("计算奖励,存入数据库");
                List<Integer> propsIdList = currDuplicate.getPropsIdList();

                GameMsg.DuplicateFinishResult.Builder newBuilder = GameMsg.DuplicateFinishResult.newBuilder();
                for (Integer id : propsIdList) {
                    newBuilder.addPropsId(id);
                }

                for (DuplicateType duplicateType : DuplicateType.values()) {
                    if (duplicateType.getName().equals(currDuplicate.getName())) {
                        newBuilder.setMoney(duplicateType.getMoney());
                        user.setMoney(user.getMoney()+duplicateType.getMoney());
                    }
                }
                // 添加数据库
                GameMsg.DuplicateFinishResult duplicateFinishResult = newBuilder.build();
                ctx.writeAndFlush(duplicateFinishResult);

                userService.modifyMoney(userId,user.getMoney());
                // 副本得到的奖励进行持久化
                addProps(propsIdList,user);

                user.setCurrDuplicate(null);
                // 用户退出
                GameMsg.UserQuitDuplicateResult userQuitDuplicateResult = GameMsg.UserQuitDuplicateResult.newBuilder().build();
                ctx.writeAndFlush(userQuitDuplicateResult);

            }
        } else {
            // 剩余血量 大于 应减少的值
            currBossMonster.setHp(currBossMonster.getHp() - subHp);
            GameMsg.AttkBossResult attkBossResult = GameMsg.AttkBossResult.newBuilder().setSubHp(subHp).build();
            ctx.writeAndFlush(attkBossResult);
        }

    }


    private void addProps(List<Integer> propsIdList, User user) {
        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        Map<Integer, Props> backpack = user.getBackpack();

        if (backpack.size() >= BackPackConst.MAX_CAPACITY) {
            // 此时背包已满，
            return;
        }

        for (Integer propsId : propsIdList) {
            Props props = propsMap.get(propsId);
            if (props.getPropsProperty().getType() == PropsType.Equipment) {
                // 此道具是 装备
                Equipment equipment = (Equipment) props.getPropsProperty();

                //封装
                UserEquipmentEntity userEquipmentEntity = new UserEquipmentEntity();
                userEquipmentEntity.setIsWear(0);
                userEquipmentEntity.setDurability(EquipmentConst.NO_WEAR);
                userEquipmentEntity.setPropsId(equipment.getPropsId());
                userEquipmentEntity.setUserId(user.getUserId());

                Equipment equ = null;
                for (int i = 1; i < BackPackConst.MAX_CAPACITY; i++) {
                    if (!backpack.keySet().contains(i)) {
                        Props pro = new Props();
                        pro.setId(propsId);
                        pro.setName(props.getName());
                        equ = new Equipment(null, pro.getId(), EquipmentConst.MAX_DURABILITY, equipment.getDamage(), equipment.getEquipmentType());
                        pro.setPropsProperty(equ);

                        backpack.put(i, pro);
                        userEquipmentEntity.setLocation(i);
                        break;
                    }
                }
                userService.addEquipment(userEquipmentEntity);
                equ.setId(userEquipmentEntity.getId());

            } else if (props.getPropsProperty().getType() == PropsType.Potion) {
                // 此道具是 药剂
                Potion potion = (Potion) props.getPropsProperty();

                boolean isExist = false;
                for (Props pro : backpack.values()) {
                    // 查询背包中是否有该药剂
                    if (potion.getId().equals(pro.getId())){
                        // 背包中已有该药剂
                        Potion po = (Potion) pro.getPropsProperty();
                        po.setNumber(po.getNumber()+1);
                        isExist = true;
                    }

                }

                // 背包中还没有该药剂
                if (!isExist){

                    UserPotionEntity userPotionEntity = new UserPotionEntity();
                    userPotionEntity.setUserId(user.getUserId());
                    userPotionEntity.setNumber(1);
                    userPotionEntity.setPropsId(propsId);

                    Potion po = null;
                    for (int i = 1; i < BackPackConst.MAX_CAPACITY; i++) {
                        if (!backpack.keySet().contains(i)) {
                            Props pro = new Props();
                            pro.setId(propsId);
                            pro.setName(props.getName());
                            po = new Potion(null,propsId,potion.getCdTime(),potion.getInfo(),potion.getResumeFigure(),potion.getPercent(),1);
                            pro.setPropsProperty(po);

                            userPotionEntity.setLocation(i);

                            backpack.put(i, pro);
                            break;
                        }
                    }
                    userService.addPotion(userPotionEntity);
                    userPotionEntity.setId(userPotionEntity.getId());
                }
            }

        }

    }

}
