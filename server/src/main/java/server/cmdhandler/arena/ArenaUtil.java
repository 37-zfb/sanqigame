package server.cmdhandler.arena;

import msg.GameMsg;
import server.model.User;

/**
 * @author 张丰博
 */
public final class ArenaUtil {

    private static final ArenaUtil ARENA_UTIL = new ArenaUtil();

    private ArenaUtil(){}

    public static ArenaUtil getArenaUtil(){
        return ARENA_UTIL;
    }

    public void sendMsg(User user,User targetUser,Integer subHp){
        GameMsg.UserSubtractHpResult userSubtractHpResult = GameMsg.UserSubtractHpResult.newBuilder()
                .setTargetUserId(targetUser.getUserId())
                .setSubtractHp(subHp)
                .build();
        user.getCtx().writeAndFlush(userSubtractHpResult);
        targetUser.getCtx().writeAndFlush(userSubtractHpResult);


        if (targetUser.getCurrHp() > 0) {
            return;
        }
        // 此时用户死了
        targetUser.getPlayArena().setTargetUserId(null);
        user.getPlayArena().setTargetUserId(null);



        GameMsg.UserDieResult userDieResult = GameMsg.UserDieResult.newBuilder()
                .setTargetUserId(targetUser.getUserId())
                .build();
        user.getCtx().writeAndFlush(userDieResult);
        targetUser.getCtx().writeAndFlush(userDieResult);
    }

}
