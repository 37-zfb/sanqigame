package server.cmdhandler.deal;

import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.UserManager;
import server.cmdhandler.ICmdHandler;
import server.model.Deal;
import server.model.DealProps;
import server.model.User;
import server.model.props.Props;
import server.scene.GameData;
import util.MyUtil;

/**
 * @author 张丰博
 * <p>
 * 取消添加好的道具
 */
@Component
@Slf4j
public class UserCancelDealItemCmdHandler implements ICmdHandler<GameMsg.UserCancelDealItemCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserCancelDealItemCmd userCancelDealItemCmd) {

        MyUtil.checkIsNull(ctx, userCancelDealItemCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        Deal deal = user.getDeal();
        if (deal == null || deal.getTargetId() == null || deal.getInitiatorId() == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_DEAL_STATUS);
        }

        if ((deal.getInitiatorId().equals(user.getUserId()) && user.getDeal().isInitiatorIsDetermine()) ||
                (deal.getTargetId().equals(user.getUserId()) && user.getDeal().isTargetIsDetermine())) {
            throw new CustomizeException(CustomizeErrorCode.PROPS_ADD_COMPLETE);
        }


        GameMsg.Props propsInfo = userCancelDealItemCmd.getProps();
        int location = propsInfo.getLocation();
        int propsId = propsInfo.getPropsId();
        int propsNumber = propsInfo.getPropsNumber();



        DealProps dealProps = null;
        if (user.getUserId() == deal.getInitiatorId()) {
            dealProps = deal.getInitiatorProps().get(location);
        }
        if (user.getUserId() == deal.getTargetId()) {
            dealProps = deal.getTargetProps().get(location);
        }


        if (dealProps == null || dealProps.getNumber() < propsNumber) {
            throw new CustomizeException(CustomizeErrorCode.DEAL_PROPS_NOT_EXIST);
        }

        if (dealProps.getNumber() == propsNumber) {
            if (user.getUserId() == deal.getInitiatorId()) {
                deal.getInitiatorProps().remove(location);
            }

            if (user.getUserId() == deal.getTargetId()) {
                deal.getTargetProps().remove(location);
            }
            log.info("用户: {} 取消 {} ",
                    user.getUserName(),
                    GameData.getInstance().getPropsMap().get(propsId).getName());
        }

        if (dealProps.getNumber() > propsNumber) {
            dealProps.setNumber(dealProps.getNumber() - propsNumber);
            log.info("用户: {} 减少 {}个 {}",
                    user.getUserName(),
                    propsNumber,
                    GameData.getInstance().getPropsMap().get(propsId).getName());
        }


        GameMsg.UserCancelDealItemResult userCancelDealItemResult = GameMsg.UserCancelDealItemResult.newBuilder()
                .setProps(propsInfo)
                .build();
        Integer targetId = null;
        if (user.getUserId() == deal.getInitiatorId()) {
            targetId = deal.getTargetId();
        }
        if (user.getUserId() == deal.getTargetId()) {
            targetId = deal.getInitiatorId();
        }
        User targetUser = UserManager.getUserById(targetId);
        targetUser.getCtx().writeAndFlush(userCancelDealItemResult);
    }
}
