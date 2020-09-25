package server.cmdhandler.deal;

import exception.CustomizeErrorCode;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.model.Deal;
import server.model.DealProps;
import server.model.PlayDeal;
import server.model.User;
import server.UserManager;
import server.model.props.Potion;
import server.model.props.Props;
import server.scene.GameData;
import type.DealInfoType;
import type.PropsType;
import util.MyUtil;

import java.util.Map;


/**
 * @author 张丰博
 * 添加 道具
 */
@Component
@Slf4j
public class UserDealItemCmdHandler implements ICmdHandler<GameMsg.UserDealItemCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.UserDealItemCmd userDealItemCmd) {

        MyUtil.checkIsNull(ctx, userDealItemCmd);
        User user = PublicMethod.getInstance().getUser(ctx);

        Deal deal = user.getDeal();
        if (deal == null || deal.getTargetId() == null || deal.getInitiatorId() == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_DEAL_STATUS);
        }

        if ((deal.getInitiatorId().equals(user.getUserId()) && user.getDeal().isInitiatorIsDetermine()) ||
                (deal.getTargetId().equals(user.getUserId()) && user.getDeal().isTargetIsDetermine())) {
            throw new CustomizeException(CustomizeErrorCode.PROPS_ADD_COMPLETE);
        }


        GameMsg.Props propsInfo = userDealItemCmd.getProps();
        int location = propsInfo.getLocation();
        Props props = user.getBackpack().get(location);
        if (props == null) {
            return;
        }

        int propsId = propsInfo.getPropsId();
        int propsNumber = propsInfo.getPropsNumber();
        if (props.getPropsProperty().getType() == PropsType.Potion &&
                ((Potion) props.getPropsProperty()).getNumber() < propsNumber) {
            throw new CustomizeException(CustomizeErrorCode.POTION_INSUFFICIENT);
        }


        //添加道具
        if (user.getUserId() == deal.getInitiatorId()) {
            Map<Integer, DealProps> initiatorProps = deal.getInitiatorProps();
            DealProps dealProps = initiatorProps.get(location);
            if (dealProps == null) {
                initiatorProps.put(location, new DealProps(propsId, propsNumber));
            } else {
                dealProps.setNumber(dealProps.getNumber() + propsNumber);
            }

        }
        if (user.getUserId() == deal.getTargetId()) {
            Map<Integer, DealProps> targetProps = deal.getTargetProps();
            DealProps dealProps = targetProps.get(location);
            if (dealProps == null) {
                targetProps.put(location, new DealProps(propsId, propsNumber));
            } else {
                dealProps.setNumber(dealProps.getNumber() + propsNumber);
            }
        }
        log.info("用户: {} 添加了 {} {}个",
                user.getUserName(),
                GameData.getInstance().getPropsMap().get(propsId).getName(),
                propsNumber);


        GameMsg.UserDealItemResult userDealItemResult = GameMsg.UserDealItemResult.newBuilder()
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
        if (targetUser == null){
            return;
        }
        targetUser.getCtx().writeAndFlush(userDealItemResult);
    }


}
