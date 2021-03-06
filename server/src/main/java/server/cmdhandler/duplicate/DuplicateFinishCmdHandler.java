package server.cmdhandler.duplicate;

import constant.DuplicateConst;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.PublicMethod;
import server.cmdhandler.ICmdHandler;
import server.cmdhandler.task.listener.TaskUtil;
import server.model.User;
import server.model.duplicate.Duplicate;
import server.util.PropsUtil;
import type.DuplicateType;
import type.TaskType;
import util.MyUtil;

import java.util.Arrays;
import java.util.List;

/**
 * @author 张丰博
 * 副本结束
 */
@Component
@Slf4j
public class DuplicateFinishCmdHandler implements ICmdHandler<GameMsg.DuplicateFinishCmd> {

    @Autowired
    private TaskUtil taskPublicMethod;

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsg.DuplicateFinishCmd duplicateFinishCmd) {

        MyUtil.checkIsNull(ctx, duplicateFinishCmd);
        User user = PublicMethod.getInstance().getUser(ctx);


        Duplicate currDuplicate = PublicMethod.getInstance().getDuplicate(user);
        if (currDuplicate == null) {
            return;
        }


        //取消自动掉血
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
        taskPublicMethod.listener(user, TaskType.DuplicateType.getTaskCode());
    }


}
