package client.cmd.mail;

import client.model.server.props.Equipment;
import client.model.server.props.Potion;
import client.model.server.props.Props;
import client.scene.GameData;
import client.thread.CmdThread;
import client.cmd.ICmd;
import client.model.MailClient;
import client.model.Role;
import client.model.SceneData;
import client.model.client.MailEntityClient;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import type.MailType;
import type.PropsType;
import util.MyUtil;

import java.util.List;
import java.util.Map;

/**
 * @author 张丰博
 */
@Slf4j
public class UserReceiveMailResultClient implements ICmd<GameMsg.UserReceiveMailResult> {
    @Override
    public void cmd(ChannelHandlerContext ctx, GameMsg.UserReceiveMailResult userReceiveMailResult) {

        MyUtil.checkIsNull(ctx, userReceiveMailResult);

        List<Long> mailIdList = userReceiveMailResult.getMailIdList();
        Role role = Role.getInstance();
        MailClient mail = role.getMail();
        Map<Long, MailEntityClient> mailMap = mail.getMailMap();
        for (Long mailId : mailIdList) {
            MailEntityClient mailEntityClient = mailMap.get(mailId);
            System.out.println(mailEntityClient);
            mailEntityClient.setMailType(MailType.READ);

        }

        role.setMoney(userReceiveMailResult.getMoney());
        List<GameMsg.Props> propsList = userReceiveMailResult.getPropsList();

        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        Map<Integer, Props> backpackClient = role.getBackpackClient();
        for (GameMsg.Props props : propsList) {
            int location = props.getLocation();
            int propsId = props.getPropsId();
            int propsNumber = props.getPropsNumber();

            Props pro = propsMap.get(propsId);
            System.out.println(pro.getName());

            if (pro.getPropsProperty().getType() == PropsType.Equipment) {

                Equipment equipment = (Equipment) pro.getPropsProperty();

                Equipment propsProperty =
                        //props.getUserPropsId() 是 表 user_equipment 中的id
                        new Equipment(props.getUserPropsId(),
                                equipment.getPropsId(),
                                props.getDurability(),
                                equipment.getDamage(),
                                equipment.getEquipmentType());

                backpackClient.put(location, new Props(pro.getId(), pro.getName(), propsProperty));
            } else if (pro.getPropsProperty().getType() == PropsType.Potion) {
                Props p = backpackClient.get(location);

                if (p != null) {
                    //已存在
                    Potion propsProperty = (Potion) p.getPropsProperty();
                    propsProperty.setNumber(propsProperty.getNumber() + propsNumber);
                    continue;
                }

                //不存在
                Potion potion = (Potion) pro.getPropsProperty();
                Potion propsProperty =
                        new Potion(props.getUserPropsId(),
                                potion.getPropsId(),
                                props.getDurability(),
                                potion.getInfo(),
                                potion.getResumeFigure(),
                                potion.getPercent(),
                                propsNumber);

                backpackClient.put(location, new Props(pro.getId(), pro.getName(), propsProperty));
            }

        }


//        backpack(role, userReceiveMailResult.getPropsList());
//        if (mailMap.size() <= 0) {
//            mail.setHave(false);
//        }


        CmdThread.getInstance().process(ctx, role, SceneData.getInstance().getSceneMap().get(role.getCurrSceneId()).getNpcMap().values());
    }

    private void backpack(Role role, List<GameMsg.Props> propsList) {
        Map<Integer, Props> propsMap = GameData.getInstance().getPropsMap();
        Map<Integer, Props> backpackClient = role.getBackpackClient();
        for (GameMsg.Props props : propsList) {
            Props pro = propsMap.get(props.getPropsId());
            if (pro.getPropsProperty().getType() == PropsType.Equipment) {
                Equipment equipment = (Equipment) pro.getPropsProperty();

                Equipment propsProperty =
                        //props.getUserPropsId() 是 表 user_equipment 中的id
                        new Equipment(props.getUserPropsId(),
                                equipment.getPropsId(),
                                props.getDurability(),
                                equipment.getDamage(),
                                equipment.getEquipmentType());

                backpackClient.put(props.getLocation(), new Props(props.getPropsId(), pro.getName(), propsProperty));
            } else if (pro.getPropsProperty().getType() == PropsType.Potion) {


                Potion potion = (Potion) pro.getPropsProperty();

                Potion propsProperty =
                        //props.getUserPropsId() 是 表 user_potion 中的id
                        new Potion(props.getUserPropsId(),
                                potion.getPropsId(),
                                potion.getCdTime(),
                                potion.getInfo(),
                                potion.getResumeFigure(),
                                potion.getPercent(),
                                props.getPropsNumber());

                backpackClient.put(props.getLocation(), new Props(props.getPropsId(), pro.getName(), propsProperty));
            }


        }
    }
}
