package server.model;

import lombok.Getter;
import lombok.Setter;
import server.model.props.Props;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 张丰博
 */
@Getter
@Setter
public class PlayDeal {


    /**
     * 标识正在和 谁 进行交易；
     * 若 为 0，则不是交易状态
     */
    private  Integer targetUserId = 0;
    /**
     * 是否放完装备
     */
    private boolean isDetermine = false;


    /**
     * 确定交易锁
     */
    private Object completeDealMonitor;
    /**
     * 同意交易个数
     */
    private int agreeNumber = 0;

    /**
     * 发起交易的用户
     */
    private final Set<Integer> userIdSet = new ConcurrentSkipListSet<>();


    /**
     * 准备交易的道具   k:location
     */
    private final Map<Integer, DealProps> prepareProps = new HashMap<>();
    /**
     * 准备交易的钱
     */
    private Integer prepareMoney = 0;

    /**
     *  接收的道具
     */
    private  final Map<Integer, DealProps> receiveProps = new ConcurrentHashMap<>();
    /**
     *  接收的金币
     */
    private volatile int receiveMoney = 0;




}
