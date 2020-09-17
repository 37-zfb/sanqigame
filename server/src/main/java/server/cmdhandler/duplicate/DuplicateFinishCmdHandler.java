package server.cmdhandler.duplicate;

import constant.DuplicateConst;
import exception.CustomizeException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.cmdhandler.auction.AuctionUtil;
import server.cmdhandler.task.listener.TaskPublicMethod;
import server.model.User;
import server.model.duplicate.Duplicate;
import server.model.props.Equipment;
import server.model.props.Potion;
import server.model.props.Props;
import server.scene.GameData;
import type.DuplicateType;
import type.PropsType;
import util.MyUtil;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 * 副本结束
 */
@Component
@Slf4j
public class DuplicateFinishCmdHandler implements ICmdHandler<GameMsg.DuplicateFinishCmd> {

    @Autowired
    private TaskPublicMethod taskPublicMethod;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.DuplicateFinishCmd duplicateFinishCmd) {

        MyUtil.checkIsNull(ctx, duplicateFinishCmd);
        User user = PublicMethod.getInstance().getUser(ctx);


        Duplicate currDuplicate = PublicMethod.getInstance().getDuplicate(user);
        if (currDuplicate == null) {
            return;
        }


        if (user.getSubHpTask() != null) {
            user.getSubHpTask().cancel(true);
            user.setSubHpNumber(0);
        }

        System.out.println("计算奖励,存入数据库");

        List<Integer> propsIdList = currDuplicate.getPropsIdList();

        GameMsg.DuplicateFinishResult.Builder newBuilder = GameMsg.DuplicateFinishResult.newBuilder();

        PropsUtil.getPropsUtil().addProps(propsIdList, user, newBuilder, DuplicateConst.PROPS_NUMBER);

        for (DuplicateType duplicateType : DuplicateType.values()) {
            if (duplicateType.getName().equals(currDuplicate.getName())) {
                newBuilder.setMoney(duplicateType.getMoney());
                user.setMoney(user.getMoney() + duplicateType.getMoney());
            }
        }

        GameMsg.DuplicateFinishResult duplicateFinishResult = newBuilder.build();
        ctx.writeAndFlush(duplicateFinishResult);

        taskPublicMethod.addExperience(DuplicateConst.DUPLICATE_EXPERIENCE, user);
        //任务监听
        taskPublicMethod.listener(user);
    }


}
