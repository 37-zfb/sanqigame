package server.model;

import entity.db.GuildEntity;
import entity.db.GuildMemberEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import server.model.props.Props;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 张丰博
 */
@Setter
@Getter
@NoArgsConstructor
@ToString
public class PlayGuild {


    /**
     * 公会id
     */
    private Integer id;

    /**
     * 公会基础信息
     */
    private GuildEntity guildEntity;

    /**
     * 用户id  公会成员
     */
    private final Map<Integer, GuildMemberEntity> guildMemberMap = new ConcurrentHashMap<>();


    /**
     * 仓库监视器
     */
    private final Object WAREHOUSE_MONITOR = new Object();
    /**
     * 仓库金币
     */
    private int warehouseMoney = 0;
    /**
     * 仓库道具  位置 道具
     */
    private final Map<Integer, Props> WAREHOUSE_PROPS = new HashMap<>();



}
