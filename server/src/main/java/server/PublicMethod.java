package server;

import constant.BackPackConst;
import constant.EquipmentConst;
import constant.PotionConst;
import entity.db.UserEquipmentEntity;
import entity.db.UserPotionEntity;
import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import model.duplicate.BossMonster;
import model.duplicate.Duplicate;
import model.props.Equipment;
import model.props.Potion;
import model.props.Props;
import msg.GameMsg;
import server.model.PlayTeam;
import server.model.User;
import server.model.UserManager;
import server.service.UserService;

import java.util.Map;

/**
 * @author 张丰博
 */
public final class PublicMethod {

    private static final PublicMethod PUBLIC_METHOD = new PublicMethod();

    private UserService userService = GameServer.APPLICATION_CONTEXT.getBean(UserService.class);

    private PublicMethod(){}

    public static PublicMethod getInstance(){
        return PUBLIC_METHOD;
    }

    /**
     *  添加装备
     * @param user
     * @param props
     * @throws CustomizeException 如果背包满了，则抛出异常
     */
    public void addEquipment(User user, Props props) {

        Map<Integer, Props> backpack = user.getBackpack();

        if (backpack.size() >= BackPackConst.MAX_CAPACITY) {
            // 此时背包已满，
            throw new CustomizeException(CustomizeErrorCode.BACKPACK_SPACE_INSUFFICIENT);
        }

        // 此道具是 装备
        Equipment equipment = (Equipment) props.getPropsProperty();

        //封装
        UserEquipmentEntity userEquipmentEntity = new UserEquipmentEntity();
        userEquipmentEntity.setIsWear(EquipmentConst.NO_WEAR);
        userEquipmentEntity.setDurability(EquipmentConst.MAX_DURABILITY);
        userEquipmentEntity.setPropsId(equipment.getPropsId());
        userEquipmentEntity.setUserId(user.getUserId());

        Equipment equ = null;
        for (int i = 1; i < BackPackConst.MAX_CAPACITY; i++) {
            if (!backpack.keySet().contains(i)) {
                Props pro = new Props();
                pro.setId(equipment.getPropsId());
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

    }

    /**
     * 添加药剂
     *
     * @param props
     * @param user
     * @param number
     *  @throws CustomizeException 如果背包中不存在此药剂且背包满了，则抛出异常；背包中此药剂数量达到上限抛出异常
     */
    public void addPotion(Props props, User user, Integer number) {

        Map<Integer, Props> backpack = user.getBackpack();

        // 此道具是 药剂
        Potion potion = (Potion) props.getPropsProperty();

        UserPotionEntity userPotionEntity = new UserPotionEntity();
        userPotionEntity.setUserId(user.getUserId());
        userPotionEntity.setPropsId(potion.getPropsId());

        boolean isExist = false;
        for (Props pro : backpack.values()) {
            // 查询背包中是否有该药剂
            if (potion.getPropsId().equals(pro.getId())) {
                // 判断该药剂的数量是否达到上限
                // 背包中已有该药剂
                Potion po = (Potion) pro.getPropsProperty();

                if ((po.getNumber()+number) > PotionConst.POTION_MAX_NUMBER){
                    throw new CustomizeException(CustomizeErrorCode.PROPS_REACH_LIMIT);
                }

                po.setNumber(po.getNumber() + number);
                isExist = true;

                userPotionEntity.setNumber(po.getNumber());
                break;
            }
        }
        // 背包中还没有该药剂
        if (!isExist) {
            if (backpack.size() >= BackPackConst.MAX_CAPACITY) {
                // 此时背包已满，
                throw new CustomizeException(CustomizeErrorCode.BACKPACK_SPACE_INSUFFICIENT);
            }

            userPotionEntity.setNumber(number);

            Potion po = null;
            for (int i = 1; i < BackPackConst.MAX_CAPACITY; i++) {
                if (!backpack.keySet().contains(i)) {
                    Props pro = new Props();
                    pro.setId(potion.getPropsId());
                    pro.setName(props.getName());
                    po = new Potion(null, potion.getPropsId(), potion.getCdTime(), potion.getInfo(), potion.getResumeFigure(), potion.getPercent(), number);
                    pro.setPropsProperty(po);

                    userPotionEntity.setLocation(i);
                    // 药剂添加进背包
                    backpack.put(i, pro);
                    break;
                }
            }
        }
        userService.addPotion(userPotionEntity);
        userPotionEntity.setId(userPotionEntity.getId());
    }


    /**
     *  获取用户
     * @param ctx
     * @return
     */
    public User getUser(ChannelHandlerContext ctx){
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        if (userId == null){
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_MANAGER);
        }
        User user = UserManager.getUserById(userId);
        if (user == null){
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_MANAGER);
        }

        return user;
    }

    /**
     *  退出队伍
     * @param user
     */
    public void quitTeam(User user){
        PlayTeam playTeam = user.getPlayTeam();
        Integer[] team_member = playTeam.getTEAM_MEMBER();
        for (int i = 0; i < team_member.length; i++) {
            if (team_member[i] != null && team_member[i].equals(user.getUserId())) {
                team_member[i] = null;
                break;
            }
        }

        if (playTeam.getTeamLeaderId().equals(user.getUserId())){
            // 如果 队长退出队伍,选择新队员成为队长
            team_member = playTeam.getTEAM_MEMBER();
            for (int i = 0; i < team_member.length; i++) {
                if (team_member[i]!=null){
                    playTeam.setTeamLeaderId(team_member[i]);
                    break;
                }
            }
        }

        Duplicate currDuplicate = playTeam.getCurrDuplicate();
        if (currDuplicate != null){
            // 若当前副本不为空
            BossMonster currBossMonster = currDuplicate.getCurrBossMonster();
            synchronized (currBossMonster.getCHOOSE_USER_MONITOR()){
                Map<Integer, Integer> userIdMap = currBossMonster.getUserIdMap();
                userIdMap.remove(user.getUserId());
            }
        }

        user.setPlayTeam(null);
        GameMsg.UserInfo.Builder userInfo = GameMsg.UserInfo.newBuilder()
                .setUserId(user.getUserId())
                .setUserName(user.getUserName());
        GameMsg.UserQuitTeamResult userQuitTeamResult = GameMsg.UserQuitTeamResult.newBuilder()
                .setUserInfo(userInfo)
                .setTeamLeaderId(playTeam.getTeamLeaderId())
                .build();
        user.getCtx().writeAndFlush(userQuitTeamResult);
        for (Integer id : team_member) {
            if (id != null){
                User userById = UserManager.getUserById(id);
                userById.getCtx().writeAndFlush(userQuitTeamResult);
            }
        }
    }

}
