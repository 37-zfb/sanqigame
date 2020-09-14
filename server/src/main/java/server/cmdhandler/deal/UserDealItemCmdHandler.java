package server.cmdhandler.deal;

import exception.CustomizeErrorCode;
import exception.CustomizeException;
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
import server.model.props.Potion;
import server.model.props.Props;
import server.scene.GameData;
import type.DealInfoType;
import type.PropsType;
import util.MyUtil;


/**
 * @author 张丰博
 * 添加或减少 道具
 */
@Component
@Slf4j
public class UserDealItemCmdHandler implements ICmdHandler<GameMsg.UserDealItemCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserDealItemCmd userDealItemCmd) {

        MyUtil.checkIsNull(ctx, userDealItemCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        PlayDeal playDeal = user.getPLAY_DEAL();
        if (playDeal.getTargetUserId().get() == 0) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_DEAL_STATUS);
        }

        if (playDeal.isDetermine()){
            throw new CustomizeException(CustomizeErrorCode.PROPS_ADD_COMPLETE);
        }

        int money = userDealItemCmd.getMoney();
        if (user.getMoney() < money) {
            throw new CustomizeException(CustomizeErrorCode.USER_MONEY_INSUFFICIENT);
        }

        GameMsg.Props propsInfo = userDealItemCmd.getProps();
        int location = propsInfo.getLocation();
        Props props = user.getBackpack().get(location);

        int propsId = propsInfo.getPropsId();
        int propsNumber = propsInfo.getPropsNumber();
        if (props != null &&
                props.getPropsProperty().getType() == PropsType.Potion &&
                ((Potion) props.getPropsProperty()).getNumber() < propsNumber) {
            throw new CustomizeException(CustomizeErrorCode.POTION_INSUFFICIENT);
        }

        String type = userDealItemCmd.getType();

        // 金币
        playDeal.setPrepareMoney(playDeal.getPrepareMoney() + money);
        log.info("用户: {} 添加 {} 金币;", user.getUserName(), money);

        if (location != 0) {
            if (type.equals(DealInfoType.ADD.getType())) {
                //添加道具
                playDeal.getPrepareProps().put(location, new DealProps(propsId, propsNumber));
                log.info("用户: {} 添加了 {} {}个", user.getUserName(), GameData.getInstance().getPropsMap().get(propsId).getName(), propsNumber);
            }

            if (type.equals(DealInfoType.CANCEL.getType())) {
                DealProps dealProps = playDeal.getPrepareProps().get(location);
                if (dealProps == null || dealProps.getNumber() < propsNumber) {
                    throw new CustomizeException(CustomizeErrorCode.DEAL_PROPS_NOT_EXIST);
                }

                if (dealProps.getNumber() == propsNumber) {
                    playDeal.getPrepareProps().remove(location);
                    log.info("用户: {} 取消 {} ", user.getUserName(), GameData.getInstance().getPropsMap().get(propsId).getName());
                }

                if (dealProps.getNumber() > propsNumber) {
                    dealProps.setNumber(dealProps.getNumber() - propsNumber);
                    log.info("用户: {} 减少 {}个 {}", user.getUserName(), propsNumber, GameData.getInstance().getPropsMap().get(propsId).getName());
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
