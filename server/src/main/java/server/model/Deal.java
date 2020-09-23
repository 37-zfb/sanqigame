package server.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 张丰博
 */
@Setter
@Getter
@NoArgsConstructor
@ToString
public class Deal {

    /**
     * 交易发起者id
     */
    private Integer initiatorId;
    /**
     * 发起者交易金币
     */
    private Integer initiatorMoney = 0;
    /**
     * 发起者准备交易的道具   k:location
     */
    private final Map<Integer, DealProps> initiatorProps = new HashMap<>();
    /**
     * 发起者是否放完装备
     */
    private boolean initiatorIsDetermine = false;



    /**
     * 目标用户id
     */
    private Integer targetId;
    /**
     * 目标用户交易金币
     */
    private Integer targetMoney = 0;
    /**
     * 目标用户准备交易的道具   k:location
     */
    private final Map<Integer, DealProps> targetProps = new HashMap<>();
    /**
     * 目标用户是否放完装备
     */
    private boolean targetIsDetermine = false;



    /**
     * 同意交易个数
     */
    private int agreeNumber = 0;
}
