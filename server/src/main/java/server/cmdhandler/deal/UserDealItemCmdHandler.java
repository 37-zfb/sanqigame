package server.cmdhandler.deal;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.DealProps;
import server.model.PlayDeal;
import server.model.User;
import server.model.UserManager;
import server.scene.GameData;
import type.DealInfoType;
import util.MyUtil;


/**
 * @author 张丰博
 * 添加或减少 道具，处理类
 */
@Component
@Slf4j
public class UserDealItemCmdHandler implements ICmdHandler<GameMsg.UserDealItemCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserDealItemCmd userDealItemCmd) {

        MyUtil.checkIsNull(ctx, userDealItemCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayDeal playDeal = user.getPLAY_DEAL();

        GameMsg.Props propsInfo = userDealItemCmd.getProps();
        int location = propsInfo.getLocation();
        int propsNumber = propsInfo.getPropsNumber();
        int propsId = propsInfo.getPropsId();
        int money = userDealItemCmd.getMoney();

        String type = userDealItemCmd.getType();
        // money!=0 此时用户添加或减少 金币的交易
        if (money != 0){
            playDeal.setPrepareMoney(playDeal.getPrepareMoney() + money);
            log.info("用户: {} 添加 {} 金币;", user.getUserName(), money);
        }else if (location != 0){
            if (type.equals(DealInfoType.ADD.getType())) {
                //添加道具
                playDeal.getPrepareProps().put(location, new DealProps(propsId, propsNumber));
                log.info("用户: {} 添加了 {} {}个", user.getUserName(), GameData.getInstance().getPropsMap().get(propsId).getName(), propsNumber);
            } else if (type.equals(DealInfoType.CANCEL.getType())) {
                DealProps dealProps = playDeal.getPrepareProps().get(location);
                if (dealProps.getNumber() == propsNumber) {
                    playDeal.getPrepareProps().remove(location);
                    log.info("用户: {} 取消 {} ", user.getUserName(), GameData.getInstance().getPropsMap().get(propsId).getName());
                } else {
                    DealProps dProps = playDeal.getPrepareProps().get(location);
                    if (dealProps.getNumber() > propsNumber) {
                        dProps.setNumber(dProps.getNumber() - propsNumber);
                    } else {
                        playDeal.getPrepareProps().remove(location);
                    }
                    log.info("用户: {} 取消 {} {}个", user.getUserName(), GameData.getInstance().getPropsMap().get(propsId).getName(), propsNumber);
                }
            }
        }

        GameMsg.UserDealItemResult userDealItemResult = GameMsg.UserDealItemResult.newBuilder()
                .setMoney(money)
                .setProps(propsInfo)
                .setType(type)
                .build();
        int targetId = playDeal.getTargetUserId().get();
        User targetUser = UserManager.getUserById(targetId);
        targetUser.getCtx().writeAndFlush(userDealItemResult);
    }
}
